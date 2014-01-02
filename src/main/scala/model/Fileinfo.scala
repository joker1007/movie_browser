package model

import skinny.orm._, feature._
import scalikejdbc._, SQLInterpolation._
import org.joda.time._
import scalax.file.Path
import scalax.io.Resource
import java.security.MessageDigest
import scala.sys.process._

case class Fileinfo(
  id: Long,
  md5: String,
  fullpath: String,
  filesize: Long,
  createdAt: DateTime,
  updatedAt: Option[DateTime] = None
) {
  def createThumbnail(percentage: Int = 10, width: Int = 240, force: Boolean = false) {
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
    val output = tempDir / s"$md5.jpg"
    Seq("ffmpegthumbnailer", "-s", s"$width", "-t", s"$percentage%", "-i", fullpath, "-o", output.path).!!
    val in = Resource.fromURL(output.toURL)
    val data = in.byteArray
    Thumbnail.createWithAttributes(
      'md5 -> md5,
      'data -> data
    )
  }
}

object Fileinfo extends SkinnyCRUDMapper[Fileinfo] with TimestampsFeature[Fileinfo] {
  override val tableName = "fileinfos"
  override val defaultAlias = createAlias("f")

  val READ_LIMIT = 1024 * 1024 * 3 // 3MB
  val EXTENSIONS = Array("mp4", "m4v", "mpg", "avi", "wmv", "ogm", "ogg", "asf", "zip", "rar")

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

    val fileinfo = createWithAttributes(
      'md5 -> sum,
      'fullpath -> file.path,
      'filesize -> file.size
    )

    Some(fileinfo)
  }
}
