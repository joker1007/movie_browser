package model

import skinny.orm._, feature._
import scalikejdbc._, SQLInterpolation._
import org.joda.time._
import scalax.file.Path
import scalax.io.Resource
import java.security.MessageDigest
import scala.sys.process._
import scala.collection.mutable.ArrayBuffer
import java.util.zip.{ZipInputStream}
import java.io.{File => JFile, FileInputStream, FileOutputStream, BufferedOutputStream}
import scala.util.control.Exception._

object IsMovie {
  def unapply(f: Fileinfo): Option[Fileinfo] = {
    if (f.isMovie)
      Some(f)
    else
      None
  }
}

object IsArchive {
  def unapply(f: Fileinfo): Option[Fileinfo] = {
    if (f.isArchive)
      Some(f)
    else
      None
  }
}

object IsZip {
  def unapply(f: Fileinfo): Option[Fileinfo] = {
    if (f.extension == "zip")
      Some(f)
    else
      None
  }
}

case class Fileinfo(
  id: Long,
  md5: String,
  fullpath: String,
  filesize: Long,
  createdAt: DateTime,
  updatedAt: Option[DateTime] = None
) {
  def isMovie(): Boolean = Fileinfo.MOVIE_EXTENSIONS.contains(extension)
  def isArchive(): Boolean = Fileinfo.ARCHIVE_EXTENSIONS.contains(extension)
  def extension: String = Path(fullpath, '/').extension.getOrElse("")

  def createThumbnail(percentage: Int = 10, width: Int = 240, count:Int = 4, force: Boolean = false) {
    val t = Thumbnail.defaultAlias
    val oThumbnail = Thumbnail.findBy(sqls.eq(t.md5, md5))

    oThumbnail match {
      case Some(thumb) =>
        if (force)
          Thumbnail.deleteById(thumb.id)
        else
          return
      case None =>
    }

    val tempDir = Path.createTempDirectory()
    this match {
      case IsMovie(f) =>
        createMovieThumbnail(tempDir, percentage, width, count) match {
          case Left(throwable) => print("Error: "); println(throwable)
          case Right(merged) =>
            val data = merged.byteArray
            createThumbnailModel(data)
        }
      case IsZip(f) =>
        createZipThumbnail(tempDir, width, count) match {
          case Left(throwable) => print("Error: "); println(throwable)
          case Right(merged) =>
            val data = merged.byteArray
            createThumbnailModel(data)
        }
      case _ =>
    }
  }

  def createMovieThumbnail(tempDir: Path, percentage: Int = 10, width: Int = 240, count:Int = 4): Either[Throwable, Path] = {
    val convertCmd: ArrayBuffer[String] = ArrayBuffer("convert", "+append")
    allCatch either {
      for (i <- 1 to count) {
        val output = tempDir / s"${md5}_$i.jpg"
        Seq("ffmpegthumbnailer", "-s", s"$width", "-t", s"${percentage * i}%", "-i", fullpath, "-o", output.path).!!
        convertCmd += output.path
      }
      val merged = tempDir / s"$md5.jpg"
      convertCmd += merged.path
      convertCmd.!!
      merged
    }
  }

  def createZipThumbnail(tempDir: Path, width: Int = 240, count:Int = 4): Either[Throwable,Path] = {
    val file = new JFile(fullpath)
    val zis = new ZipInputStream(new FileInputStream(file))
    allCatch either {
      using(zis) {zs =>
        val iter = Iterator.continually(zs.getNextEntry()).filterNot(_.isDirectory()).filter {e =>
          Fileinfo.IMAGE_EXTENSIONS.contains(Path(e.getName(), '/').extension.getOrElse(""))
        }.filterNot {e =>
          val name = e.getName()
          val invalidPattern = """((^\.)|(__MACOSX)|(DS_Store))""".r
          invalidPattern.findFirstIn(name).isDefined
        }.take(count).zipWithIndex

        val convertCmd: ArrayBuffer[String] = ArrayBuffer("convert", "+append")
        for ((e, idx) <- iter) {
          val ext = Path(e.getName, '/').extension.get
          val output = tempDir / s"${md5}_$idx.$ext"
          val fos = new BufferedOutputStream(new FileOutputStream(new JFile(output.path)))
          using(fos) {os =>
            Iterator.continually(zs.read()).takeWhile(_ != -1).foreach(os write _)
          }
          convertCmd += output.path
          val mogrifyCmd: List[String] = List("mogrify", "-resize", s"${width}x", output.path)
          mogrifyCmd.!!
          println(output.path)
        }
        val merged = tempDir / s"$md5.jpg"
        convertCmd += merged.path
        convertCmd.!!
        merged
      }
    }
  }

  private[this] def createThumbnailModel(data: Array[Byte]) = Thumbnail.createWithAttributes(
    'md5 -> md5,
    'data -> data
  )
}

object Fileinfo extends SkinnyCRUDMapper[Fileinfo] with TimestampsFeature[Fileinfo] {
  override val tableName = "fileinfos"
  override val defaultAlias = createAlias("f")

  val PER_PAGE = 200
  val READ_LIMIT = 1024 * 1024 * 3 // 3MB
  val MOVIE_EXTENSIONS = Array("mp4", "m4v", "mpg", "avi", "wmv", "ogm", "ogg", "asf")
  val ARCHIVE_EXTENSIONS = Array("zip", "rar")
  val IMAGE_EXTENSIONS = Array("jpg", "jpeg", "png")
  val EXTENSIONS = MOVIE_EXTENSIONS ++ ARCHIVE_EXTENSIONS

  override def extract(rs: WrappedResultSet, rn: ResultName[Fileinfo]): Fileinfo = new Fileinfo(
    id = rs.long(rn.id),
    md5 = rs.string(rn.md5),
    fullpath = rs.string(rn.fullpath),
    filesize = rs.long(rn.filesize),
    createdAt = rs.dateTime(rn.createdAt),
    updatedAt = rs.dateTimeOpt(rn.updatedAt)
  )

  def createFromFile(file: Path): Option[Long] = {
    if (!file.canRead)
      return None

    if (!EXTENSIONS.contains(file.extension.getOrElse("")))
      return None

    val in = Resource.fromURL(file.toURL)
    val bytes = in.bytes.take(READ_LIMIT).toArray
    val md5 = MessageDigest.getInstance("MD5")
    md5.reset()
    md5.update(bytes)
    val sum = md5.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}

    val f = defaultAlias
    if (findBy(sqls.eq(f.md5, sum)).isDefined)
      return None

    val fileinfoId = createWithAttributes(
      'md5 -> sum,
      'fullpath -> file.path,
      'filesize -> file.size
    )

    val fileinfo = findById(fileinfoId).get
    fileinfo.createThumbnail()

    Some(fileinfoId)
  }

  def searchByPath(pathString: String, page: Int = 1): List[Fileinfo] = {
    findAllByPaging(sqls.like(defaultAlias.fullpath, s"%$pathString%"), PER_PAGE, (page - 1) * PER_PAGE, sqls"${defaultAlias.fullpath} asc")
  }
}
