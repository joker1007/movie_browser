package model

import skinny.orm._, feature._
import scalikejdbc._
import org.joda.time._

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
  updatedAt: DateTime
)

object Media extends SkinnyCRUDMapper[Media] with TimestampsFeature[Media] {
  override lazy val tableName = "medias"
  override lazy val defaultAlias = createAlias("m")

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
}
