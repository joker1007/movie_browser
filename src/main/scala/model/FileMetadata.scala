package model

import skinny.orm._, feature._
import scalikejdbc._, SQLInterpolation._
import org.joda.time._

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
    for {
      info <- itemInfos
      if (info.kind == "keyword")
    } yield info.name
  }

  def maker: Option[String] = {
    (for {
      info <- itemInfos
      if (info.kind == "maker")
    } yield info.name).headOption
  }

  def label: Option[String] = {
    (for {
      info <- itemInfos
      if (info.kind == "label")
    } yield info.name).headOption
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
