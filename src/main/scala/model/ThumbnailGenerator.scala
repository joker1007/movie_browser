package model

import java.io.{BufferedOutputStream, File, FileInputStream, FileOutputStream}
import java.nio.file.{Files, Path}

import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.io.FilenameUtils
import scalikejdbc._

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.sys.process._
import scalaz.Scalaz._
import scalaz._

trait ThumbnailGenerationFailure

case object ThumbnailGenerationProcessFailure extends ThumbnailGenerationFailure
case object ThumbnailAlreadyExist extends ThumbnailGenerationFailure
case object UnsupportedMedia extends ThumbnailGenerationFailure

class ThumbnailGenerator(media: Media) {
  def createThumbnail(percentage: Int = 10, width: Int = 240, count:Int = 4, force: Boolean = false): ThumbnailGenerationFailure \/ Long = {
    val t = Thumbnail.defaultAlias
    val oThumbnail = Thumbnail.findBy(sqls.eq(t.md5, media.md5))

    oThumbnail.foreach {(thumb) =>
        if (force)
          Thumbnail.deleteById(thumb.id)
        else
          return ThumbnailAlreadyExist.left
    }

    def __createThumbnailModel(result: ThumbnailGenerationFailure \/ Path): ThumbnailGenerationFailure \/ Long  = {
      result.bimap(
        {failure => print(s"Error: $failure"); failure },
        {merged =>
          val source = Source.fromFile(merged.toFile)(scala.io.Codec.ISO8859)
          val data = source.map(_.toByte).toArray
          createThumbnailModel(data)
        }
      )
    }

    val tempDir = Files.createTempDirectory("create_thumbnail")
    media match {
      case IsMovie(f) =>
        __createThumbnailModel(createMovieThumbnail(tempDir, percentage, width, count))
      case IsZip(f) =>
        __createThumbnailModel(createZipThumbnail(tempDir, width, count))
      case _ =>
        UnsupportedMedia.left
    }
  }

  def createMovieThumbnail(tempDir: Path, percentage: Int = 10, width: Int = 240, count:Int = 4): ThumbnailGenerationFailure \/ Path = {
    val convertCmd: ArrayBuffer[String] = ArrayBuffer("convert", "+append")

    try {
      for (i <- 1 to count) {
        val output = tempDir.resolve(s"${media.md5}_$i.jpg")
        Seq("ffmpegthumbnailer", "-s", s"$width", "-t", s"${percentage * i}%", "-i", media.fullpath, "-o", output.toString).!!
        convertCmd += output.toString
      }
      val merged = tempDir.resolve(s"${media.md5}.jpg")
      convertCmd += merged.toString
      convertCmd.!!
      merged.right
    } catch {
      case _: Throwable =>
        ThumbnailGenerationProcessFailure.left
    }
  }

  def createZipThumbnail(tempDir: Path, width: Int = 240, count:Int = 4): ThumbnailGenerationFailure \/ Path = {
    val file = new File(media.fullpath)
    val zis = new ZipArchiveInputStream(new FileInputStream(file), "Windows-31J")
    try {
      using(zis) {zs =>
        val iter = Iterator.continually(zs.getNextZipEntry).takeWhile(_ != null).filterNot(_.isDirectory).filter {e =>
          Media.IMAGE_EXTENSIONS.contains(FilenameUtils.getExtension(e.getName))
        }.filterNot {e =>
          val invalidPattern = """((^\.)|(__MACOSX)|(DS_Store))""".r
          invalidPattern.findFirstIn(e.getName).isDefined
        }.take(count).zipWithIndex

        val convertCmd: ArrayBuffer[String] = ArrayBuffer("convert", "+append")
        for ((e, idx) <- iter) {
          val ext = FilenameUtils.getExtension(e.getName)
          val output = tempDir.resolve(s"${media.md5}_$idx.$ext")
          val fos = new BufferedOutputStream(new FileOutputStream(new File(output.toString)))
          using(fos) {os =>
            Iterator.continually(zs.read()).takeWhile(_ != -1).foreach(os.write)
          }
          convertCmd += output.toString
          val mogrifyCmd: List[String] = List("mogrify", "-resize", s"${width}x", output.toString)
          mogrifyCmd.!!
          println(output.toString)
        }
        val merged = tempDir.resolve(s"${media.md5}.jpg")
        convertCmd += merged.toString
        convertCmd.!!
        merged.right
      }
    } catch {
      case _: Throwable =>
        ThumbnailGenerationProcessFailure.left
    }
  }

  private[this] def createThumbnailModel(data: Array[Byte]): Long = Thumbnail.createWithAttributes(
    'md5 -> media.md5,
    'data -> data
  )
}

