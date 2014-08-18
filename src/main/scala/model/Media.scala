package model

import java.io.File
import java.nio.file.Paths
import java.security.MessageDigest

import org.apache.commons.io.FilenameUtils
import org.joda.time._
import scalikejdbc._
import skinny.orm._
import skinny.orm.feature._

import scala.io.Source

// If your model has +23 fields, switch this to normal class and mixin scalikejdbc.EntityEquality.
case class Media(
  id: Long,
  md5: String,
  fullpath: String,
  relativePath: String,
  basename: String,
  filesize: Long,
  targetId: Long,
  createdAt: DateTime,
  updatedAt: DateTime,
  target: Option[Target] = None
)

object Media extends SkinnyCRUDMapper[Media] with TimestampsFeature[Media] {
  override lazy val tableName = "medias"
  override lazy val defaultAlias = createAlias("m")

  val READ_LIMIT = 1024 * 1024 * 3 // 3MB
  val MOVIE_EXTENSIONS = Array("mp4", "m4v", "mpg", "avi", "wmv", "ogm", "ogg", "asf", "mkv", "mov", "flv")
  val ARCHIVE_EXTENSIONS = Array("zip", "rar")
  val IMAGE_EXTENSIONS = Array("jpg", "jpeg", "png")
  val EXTENSIONS = MOVIE_EXTENSIONS ++ ARCHIVE_EXTENSIONS

  belongsTo[Target](Target, (m, t) => m.copy(target = t)).byDefault

  override def extract(rs: WrappedResultSet, rn: ResultName[Media]): Media = new Media(
    id = rs.get(rn.id),
    md5 = rs.get(rn.md5),
    fullpath = rs.get(rn.fullpath),
    relativePath = rs.get(rn.relativePath),
    basename = rs.get(rn.basename),
    filesize = rs.get(rn.filesize),
    targetId = rs.get(rn.targetId),
    createdAt = rs.get(rn.createdAt),
    updatedAt = rs.get(rn.updatedAt)
  )

  def createByTargetAndFile(target: Target, file: File): Option[Long] = {
    if (!file.canRead)
      return None

    val extension = FilenameUtils.getExtension(file.getName)
    if (!EXTENSIONS.contains(extension))
      return None

    val sum = getMD5Sum(file)

    val pathBase = Paths.get(target.fullpath)
    val relativePath = pathBase.relativize(file.toPath)

    val id = createWithAttributes(
      'md5 -> sum,
      'fullpath -> file.getAbsolutePath,
      'relativePath -> relativePath.toString,
      'basename -> file.getName,
      'filesize -> file.length(),
      'targetId -> target.id
    )
    Some(id)
  }

  private [this] def getMD5Sum(file: File): String = {
    val source = Source.fromFile(file)(scala.io.Codec.ISO8859)
    val bytes = source.take(READ_LIMIT).map(_.toByte).toArray
    source.close()

    val md5 = MessageDigest.getInstance("MD5")
    md5.reset()
    md5.update(bytes)
    md5.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}
  }
}
