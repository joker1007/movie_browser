package model

import skinny.orm._, feature._
import scalikejdbc._, SQLInterpolation._
import org.joda.time._

case class ItemInfo(
  id: Long,
  dmmId: String,
  kind: String,
  name: String,
  createdAt: DateTime,
  updatedAt: Option[DateTime] = None
)

object ItemInfo extends SkinnyCRUDMapper[ItemInfo] with TimestampsFeature[ItemInfo] {
  override val tableName = "item_infos"
  override val defaultAlias = createAlias("ii")

  override def extract(rs: WrappedResultSet, rn: ResultName[ItemInfo]): ItemInfo = new ItemInfo(
    id = rs.long(rn.id),
    dmmId = rs.string(rn.dmmId),
    kind = rs.string(rn.kind),
    name = rs.string(rn.name),
    createdAt = rs.dateTime(rn.createdAt),
    updatedAt = rs.dateTimeOpt(rn.updatedAt)
  )
}
