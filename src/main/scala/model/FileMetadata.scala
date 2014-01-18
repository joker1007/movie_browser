package model

import skinny.orm._, feature._
import scalikejdbc._, SQLInterpolation._
import org.joda.time._
import org.apache.commons.io.FilenameUtils

case class FileMetadata(
  id: Long,
  md5: String,
  title: String,
  url: Option[String],
  imageUrl: Option[String] = None,
  largeImageUrl: Option[String] = None,
  createdAt: DateTime,
  updatedAt: Option[DateTime] = None,
  itemInfos: Seq[ItemInfo] = Nil
) {
  def actors: Seq[String] = {
    for {
      info <- itemInfos
      if info.kind == "actress" || info.kind == "actor"
      if !(info.dmmId contains "_ruby")
      if info.name != "av"
    } yield info.name
  }

  def keywords: Seq[String] = {
    listDataInfo("keyword")
  }

  def maker: Option[String] = {
    singleDataInfo("maker")
  }

  def label: Option[String] = {
    singleDataInfo("label")
  }

  private[this] def singleDataInfo(kind: String): Option[String] = {
    (for {
      info <- itemInfos
      if info.kind == kind
    } yield info.name).headOption
  }

  private[this] def listDataInfo(kind: String): Seq[String] = {
    for {
      info <- itemInfos
      if info.kind == kind
    } yield info.name
  }

  def mkName(prefix: String, extension: String): String = {
    val makerName = maker match {
      case Some(m) => s" [$m] "
      case None => ""
    }

    val actorsName = actors match {
      case x@a :: as => s" - ${x.mkString(" ")}"
      case Nil => ""
    }

    s"$prefix$makerName$title$actorsName.$extension"
  }
}

object FileMetadata extends SkinnyCRUDMapper[FileMetadata] with TimestampsFeature[FileMetadata] {
  override val tableName = "file_metadatas"
  override val defaultAlias = createAlias("fm")

  val infos = hasManyThrough[ItemInfo](MetadataItemInfo, ItemInfo, (fm, iis) => fm.copy(itemInfos = iis)).byDefault

  override def extract(rs: WrappedResultSet, rn: ResultName[FileMetadata]): FileMetadata = new FileMetadata(
    id = rs.long(rn.id),
    md5 = rs.string(rn.md5),
    title = rs.string(rn.title),
    url = rs.stringOpt(rn.url),
    imageUrl = rs.stringOpt(rn.imageUrl),
    largeImageUrl = rs.stringOpt(rn.largeImageUrl),
    createdAt = rs.dateTime(rn.createdAt),
    updatedAt = rs.dateTimeOpt(rn.updatedAt)
  )
}
