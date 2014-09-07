package model

import scalikejdbc._
import skinny.Pagination
import skinny.orm._
import skinny.orm.feature._

trait MediaOrdering {
  val m = Media.defaultAlias
  def ordering: SQLSyntax
}

case object MediaFullpathASC extends MediaOrdering {
  def ordering: SQLSyntax = m.fullpath
}
case object MediaFullpathDESC extends MediaOrdering {
  def ordering: SQLSyntax = m.fullpath.desc
}
case object MediaBasenameASC extends MediaOrdering {
  def ordering: SQLSyntax = m.basename
}
case object MediaBasenameDESC extends MediaOrdering {
  def ordering: SQLSyntax = m.basename.desc
}
case object MediaCreatedAtASC extends MediaOrdering {
  def ordering: SQLSyntax = m.createdAt
}
case object MediaCreatedAtDESC extends MediaOrdering {
  def ordering: SQLSyntax = m.createdAt.desc
}

object MediaRepository {
  def list(pageSize: Int, pageNo: Int, ordering: Seq[MediaOrdering] = Seq(MediaBasenameASC)): List[Media] = {
    val pagination = Pagination(pageSize, pageNo)
    val sqlOrdering = ordering.map(_.ordering)
    Media.findAllWithPagination(pagination, sqlOrdering)
  }
}
