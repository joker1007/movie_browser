package model

import skinny.orm._, feature._
import scalikejdbc._, SQLInterpolation._
import org.joda.time._

// If your model has +23 fields, switch this to normal class and mixin scalikejdbc.EntityEquality.
case class Favorite(
  id: Long,
  fileinfoId: Long,
  fileinfo: Option[Fileinfo] = None,
  createdAt: DateTime,
  updatedAt: DateTime
)

object Favorite extends SkinnyCRUDMapper[Favorite] with TimestampsFeature[Favorite] {

  override lazy val defaultAlias = createAlias("fav")

  belongsTo[Fileinfo](Fileinfo, (fav, fi) => fav.copy(fileinfo = fi)).byDefault

  override def extract(rs: WrappedResultSet, rn: ResultName[Favorite]): Favorite = new Favorite(
    id = rs.get(rn.id),
    fileinfoId = rs.get(rn.fileinfoId),
    createdAt = rs.get(rn.createdAt),
    updatedAt = rs.get(rn.updatedAt)
  )
}
