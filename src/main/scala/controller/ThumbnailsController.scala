package controller

import skinny._
import model.Thumbnail

import scalikejdbc._, SQLInterpolation._


class ThumbnailsController extends SkinnyController {
  protectFromForgery()
  addMimeMapping("image/jpeg", "jpg")

  def show = params.getAs[String]("hash").map {hash =>
    contentType = "image/jpeg"
    val t = Thumbnail.defaultAlias
    Thumbnail.findBy(sqls.eq(t.md5, hash)) map (_.data) getOrElse haltWithBody(404)
  }.getOrElse(haltWithBody(404))

  private[this] implicit val skinnyController: SkinnyController = this
}
