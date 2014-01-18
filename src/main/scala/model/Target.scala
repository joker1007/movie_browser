package model

import skinny.orm._, feature._
import scalikejdbc._, SQLInterpolation._
import org.joda.time._
import scalax.file.Path

case class Target(
  id: Long,
  fullpath: String,
  asPath: Option[String] = None,
  lastUpdatedAt: Option[DateTime] = None,
  createdAt: DateTime,
  updatedAt: Option[DateTime] = None
) {
  lazy val path: Path = {
    Path(fullpath, '/')
  }
  lazy val asPathPresence: Option[String] = for {
    as <- asPath
    if (!as.isEmpty)
  } yield as
}

object Target extends SkinnyCRUDMapper[Target] with TimestampsFeature[Target] {
  override val tableName = "targets"
  override val defaultAlias = createAlias("tr")

  override def extract(rs: WrappedResultSet, rn: ResultName[Target]): Target = new Target(
    id = rs.long(rn.id),
    fullpath = rs.string(rn.fullpath),
    asPath = rs.stringOpt(rn.asPath),
    lastUpdatedAt = rs.dateTimeOpt(rn.lastUpdatedAt),
    createdAt = rs.dateTime(rn.createdAt),
    updatedAt = rs.dateTimeOpt(rn.updatedAt)
  )
}
