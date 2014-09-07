package model

import skinny.orm._, feature._
import scalikejdbc._
import org.joda.time._

// If your model has +23 fields, switch this to normal class and mixin scalikejdbc.EntityEquality.
case class Thumbnail(
  id: Long,
  md5: String,
  data: Array[Byte],
  createdAt: DateTime,
  updatedAt: DateTime
)

object Thumbnail extends SkinnyCRUDMapper[Thumbnail] with TimestampsFeature[Thumbnail] {
  override lazy val tableName = "thumbnails"
  override lazy val defaultAlias = createAlias("t")

  override def extract(rs: WrappedResultSet, rn: ResultName[Thumbnail]): Thumbnail = {
    val b = rs.blob(rn.data)
    new Thumbnail(
      id = rs.get(rn.id),
      md5 = rs.get(rn.md5),
      data = b.getBytes(1, b.length().toInt),
      createdAt = rs.get(rn.createdAt),
      updatedAt = rs.get(rn.updatedAt)
    )
  }
}