package model

import skinny.orm._, feature._
import scalikejdbc._
import org.joda.time._

// If your model has +23 fields, switch this to normal class and mixin scalikejdbc.EntityEquality.
case class Target(
  id: Long,
  fullpath: String,
  asPath: Option[String] = None,
  lastUpdatedAt: Option[DateTime] = None,
  createdAt: DateTime,
  updatedAt: DateTime
)

object Target extends SkinnyCRUDMapper[Target] with TimestampsFeature[Target] {
  override lazy val tableName = "targets"
  override lazy val defaultAlias = createAlias("t")

  override def extract(rs: WrappedResultSet, rn: ResultName[Target]): Target = new Target(
    id = rs.get(rn.id),
    fullpath = rs.get(rn.fullpath),
    asPath = rs.get(rn.asPath),
    lastUpdatedAt = rs.get(rn.lastUpdatedAt),
    createdAt = rs.get(rn.createdAt),
    updatedAt = rs.get(rn.updatedAt)
  )
}
