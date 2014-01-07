package model

import skinny.orm._, feature._
import scalikejdbc._, SQLInterpolation._
import org.joda.time._

case class Target(
  id: Long,
  fullpath: String,
  lastUpdatedAt: Option[DateTime] = None,
  createdAt: DateTime,
  updatedAt: Option[DateTime] = None
)

object Target extends SkinnyCRUDMapper[Target] with TimestampsFeature[Target] {
  override val tableName = "targets"
  override val defaultAlias = createAlias("tr")

  override def extract(rs: WrappedResultSet, rn: ResultName[Target]): Target = new Target(
    id = rs.long(rn.id),
    fullpath = rs.string(rn.fullpath),
    lastUpdatedAt = rs.dateTimeOpt(rn.lastUpdatedAt),
    createdAt = rs.dateTime(rn.createdAt),
    updatedAt = rs.dateTimeOpt(rn.updatedAt)
  )
}
