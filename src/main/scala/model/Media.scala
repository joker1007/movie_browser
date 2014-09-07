package model

import java.io.File
import java.nio.file.Paths
import java.security.MessageDigest

import com.typesafe.scalalogging.LazyLogging
import org.apache.commons.io.FilenameUtils
import org.joda.time._
import scalikejdbc._
import skinny.orm._
import skinny.orm.feature._

import scala.io.Source

import scalaz._
import Scalaz._

object IsMovie {
  def unapply(f: Media): Option[Media] = {
    if (f.isMovie)
      Some(f)
    else
      None
  }
}

object IsArchive {
  def unapply(f: Media): Option[Media] = {
    if (f.isArchive)
      Some(f)
    else
      None
  }
}

object IsZip {
  def unapply(f: Media): Option[Media] = {
    if (f.extension == "zip")
      Some(f)
    else
      None
  }
}

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
) extends LazyLogging {
  def isMovie: Boolean = Media.MOVIE_EXTENSIONS.contains(extension)

  def isArchive: Boolean = Media.ARCHIVE_EXTENSIONS.contains(extension)

  def extension: String = FilenameUtils.getExtension(fullpath)

  def createThumbnail(percentage: Int = 10, width: Int = 240, count:Int = 4, force: Boolean = false) {
    val g = new ThumbnailGenerator(this)
    val result = g.createThumbnail(percentage, width, count, force)
    if (result.isLeft) {
      val -\/(f) = result
      logger.info(s"$f")
    }
  }
}

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

  def bulkCreateByAllTargets(): List[Long] = {
    Target.findAllModels().flatMap(bulkCreateByTarget)
  }

  def bulkCreateByTarget(target: Target): List[Long] = {
    FileGather.gather(target).par.flatMap {pair =>
      val idOption = createByTargetAndFile(pair._1, pair._2)
      idOption.toList
    }.toList
  }

  def createByTargetAndFile(target: Target, file: File): Option[Long] = {
    if (!file.canRead) {
      logger.info(s"$file is not found.")
      return None
    }

    val extension = FilenameUtils.getExtension(file.getName)
    if (!EXTENSIONS.contains(extension)) {
      logger.info(s"Extension of $file is not supported.")
      return None
    }

    val sum = getMD5Sum(file)
    val fullpath = file.getAbsolutePath
    val basename = file.getName
    val filesize = file.length()

    val pathBase = Paths.get(target.fullpath)
    val relativePath = pathBase.relativize(file.toPath).toString

    val id = findBy(sqls.eq(defaultAlias.column("fullpath"), fullpath)) match {
      case Some(m) =>
        m.createThumbnail()
        m.id
      case None =>
        val createdId = createWithAttributes(
          'md5 -> sum,
          'fullpath -> fullpath,
          'relativePath -> relativePath,
          'basename -> basename,
          'filesize -> filesize,
          'targetId -> target.id
        )
        val m = Media(createdId, sum, fullpath, relativePath, basename, filesize, target.id, DateTime.now(), DateTime.now())
        m.createThumbnail()
        createdId
    }

    Some(id)
  }

  private [this] def getMD5Sum(file: File): String = {
    val source = Source.fromFile(file)(scala.io.Codec.ISO8859)
    val bytes = using(source) {s =>
      s.take(READ_LIMIT).map(_.toByte).toArray
    }

    val md5 = MessageDigest.getInstance("MD5")
    md5.reset()
    md5.update(bytes)
    md5.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}
  }
}
