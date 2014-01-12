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
  updatedAt: Option[DateTime] = None
)

object FileMetadata extends SkinnyCRUDMapper[FileMetadata] with TimestampsFeature[FileMetadata] {

  override val defaultAlias = createAlias("fm")
  override def tableName = "file_metadatas"

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
