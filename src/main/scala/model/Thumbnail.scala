package model

import skinny.orm._, feature._
import scalikejdbc._, SQLInterpolation._
import org.joda.time._
import java.sql.Blob

case class Thumbnail(
  id: Long,
  md5: String,
  data: Array[Byte],
  createdAt: DateTime,
  updatedAt: Option[DateTime] = None
)

object Thumbnail extends SkinnyCRUDMapper[Thumbnail] with TimestampsFeature[Thumbnail] {
  override val tableName = "thumbnails"
  override val defaultAlias = createAlias("th")

  override def extract(rs: WrappedResultSet, rn: ResultName[Thumbnail]): Thumbnail = {
    val b = rs.blob(rn.data)
    new Thumbnail(
      id = rs.long(rn.id),
      md5 = rs.string(rn.md5),
      data = b.getBytes(1, b.length().toInt),
      createdAt = rs.dateTime(rn.createdAt),
      updatedAt = rs.dateTimeOpt(rn.updatedAt)
    )
  }
}
