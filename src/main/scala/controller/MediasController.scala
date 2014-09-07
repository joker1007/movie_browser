package controller

import argonaut.Argonaut._
import argonaut._
import model.{Media, MediaRepository}
import skinny._

class MediasController extends SkinnyController with ApplicationController {
  protectFromForgery()

  val pageSize = 100

  def index = {
    val pageNo = params.getAs[Int]("page").getOrElse(1)
    val totalCount = Media.countAllModels()
    val medias = MediaRepository.list(pageSize, pageNo)
    contentType = Format.JSON.contentType

    val jMedias = medias.foldRight(Json.array()) {(m, j) =>
      val json =
        Json(
          "id" := m.id,
          "md5" := m.md5,
          "fullpath" := m.fullpath,
          "basename" := m.basename,
          "filesize" := m.filesize,
          "thumbnailUrl" := url(Controllers.thumbnails.showUrl, "hash" -> m.md5)
        )
      json -->>: j
    }

    val response = Json(
      "totalCount" := totalCount,
      "items" := jMedias
    )

    response.toString()
  }
}
