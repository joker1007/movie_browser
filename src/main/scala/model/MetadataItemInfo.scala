package model

import skinny.orm._, feature._
import scalikejdbc._, SQLInterpolation._
import org.joda.time._

case class MetadataItemInfo(
  fileMetadataId: Long,
  itemInfoId: Long
)

object MetadataItemInfo extends SkinnyJoinTable[MetadataItemInfo] {
  override val tableName = "metadata_item_infos"
  override val defaultAlias = createAlias("mii")
}
