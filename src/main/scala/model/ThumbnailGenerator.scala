package model

import scalikejdbc.SQLInterpolation._
import scalax.file.Path
import scala.collection.mutable.ArrayBuffer
import scala.util.control.Exception._
import java.io.{FileOutputStream, BufferedOutputStream, FileInputStream, File => JFile}
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import scalikejdbc._
import scala.Some
import scala.sys.process._

class ThumbnailGenerator(fileinfo: Fileinfo) {
  def createThumbnail(percentage: Int = 10, width: Int = 240, count:Int = 4, force: Boolean = false) {
    val t = Thumbnail.defaultAlias
    val oThumbnail = Thumbnail.findBy(sqls.eq(t.md5, fileinfo.md5))

    oThumbnail match {
      case Some(thumb) =>
        if (force)
          Thumbnail.deleteById(thumb.id)
        else
          return
      case None =>
    }

    def __createThumbnailModel(result: Either[Throwable, Path]) {
      result match {
        case Left(throwable) => print("Error: "); println(throwable); throwable.printStackTrace()
        case Right(merged) =>
          val data = merged.byteArray
          createThumbnailModel(data)
      }
    }

    val tempDir = Path.createTempDirectory()
    fileinfo match {
      case IsMovie(f) =>
        __createThumbnailModel(createMovieThumbnail(tempDir, percentage, width, count))
      case IsZip(f) =>
        __createThumbnailModel(createZipThumbnail(tempDir, width, count))
      case _ =>
    }
  }

  def createMovieThumbnail(tempDir: Path, percentage: Int = 10, width: Int = 240, count:Int = 4): Either[Throwable, Path] = {
    val convertCmd: ArrayBuffer[String] = ArrayBuffer("convert", "+append")
    allCatch either {
      for (i <- 1 to count) {
        val output = tempDir / s"${fileinfo.md5}_$i.jpg"
        Seq("ffmpegthumbnailer", "-s", s"$width", "-t", s"${percentage * i}%", "-i", fileinfo.fullpath, "-o", output.path).!!
        convertCmd += output.path
      }
      val merged = tempDir / s"${fileinfo.md5}.jpg"
      convertCmd += merged.path
      convertCmd.!!
      merged
    }
  }

  def createZipThumbnail(tempDir: Path, width: Int = 240, count:Int = 4): Either[Throwable,Path] = {
    val file = new JFile(fileinfo.fullpath)
    val zis = new ZipArchiveInputStream(new FileInputStream(file), "Windows-31J")
    allCatch either {
      using(zis) {zs =>
        val iter = Iterator.continually(zs.getNextZipEntry()).takeWhile(_ != null).filterNot(_.isDirectory()).filter {e =>
          Fileinfo.IMAGE_EXTENSIONS.contains(Path(e.getName(), '/').extension.getOrElse(""))
        }.filterNot {e =>
          val invalidPattern = """((^\.)|(__MACOSX)|(DS_Store))""".r
          invalidPattern.findFirstIn(e.getName()).isDefined
        }.take(count).zipWithIndex

        val convertCmd: ArrayBuffer[String] = ArrayBuffer("convert", "+append")
        for ((e, idx) <- iter) {
          val ext = Path(e.getName, '/').extension.get
          val output = tempDir / s"${fileinfo.md5}_$idx.$ext"
          val fos = new BufferedOutputStream(new FileOutputStream(new JFile(output.path)))
          using(fos) {os =>
            Iterator.continually(zs.read()).takeWhile(_ != -1).foreach(os write _)
          }
          convertCmd += output.path
          val mogrifyCmd: List[String] = List("mogrify", "-resize", s"${width}x", output.path)
          mogrifyCmd.!!
          println(output.path)
        }
        val merged = tempDir / s"${fileinfo.md5}.jpg"
        convertCmd += merged.path
        convertCmd.!!
        merged
      }
    }
  }

  private[this] def createThumbnailModel(data: Array[Byte]) = Thumbnail.createWithAttributes(
    'md5 -> fileinfo.md5,
    'data -> data
  )
}
