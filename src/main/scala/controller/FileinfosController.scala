package controller

import skinny._
import skinny.validator._
import model.{StandardMP4Encoder, MovieEncoder, HttpLiveStreamingEncoder, Fileinfo}
import scalikejdbc._, SQLInterpolation._
import java.io.FileInputStream
import java.net.URLEncoder
import scala.collection.JavaConversions._

object FileinfosController extends SkinnyResource {
  protectFromForgery()
  override def scalateExtension = "scaml"
  override def model = Fileinfo
  override def resourcesName = "fileinfos"
  override def resourceName = "fileinfo"

  addMimeMapping("video/mp4", "mp4")
  addMimeMapping("video/webm", "webm")
  addMimeMapping("application/octet-stream", "avi")

  override def showResources()(implicit format: Format = Format.HTML): Any = withFormat(format) {
    val query = params.getAs[String]("q")
    set("query", query)

    val page = params.getAs[Int]("page") getOrElse 1
    set("currentPage", page)

    val (resources, count) = model.joinAllByPagingWithTotalCount(page = page, searchWord = query)
    set(resourcesName, resources)
    set("fileinfosCount", count)

    render(s"/${resourcesName}/index")
  }

  override def createForm = validation(createParams,
    paramKey("md5") is required & maxLength(512),
    paramKey("fullpath") is required & maxLength(512),
    paramKey("filesize") is required & numeric & longValue
  )
  override def createParams = Params(params)
    
  override def createFormStrongParameters = Seq(
    "md5" -> ParamType.String,
    "fullpath" -> ParamType.String,
    "filesize" -> ParamType.Long
  )

  override def updateForm = validation(updateParams,
    paramKey("md5") is required & maxLength(512),
    paramKey("fullpath") is required & maxLength(512),
    paramKey("filesize") is required & numeric & longValue
  )
  override def updateParams = Params(params)
    
  override def updateFormStrongParameters = Seq(
    "md5" -> ParamType.String,
    "fullpath" -> ParamType.String,
    "filesize" -> ParamType.Long
  )

  def download = params.getAs[Long]("id").map {id =>
    Fileinfo.findById(id).map {fi =>
      val file = new java.io.File(fi.fullpath)
      if (file.toPath.getFileName.toString.endsWith("mp4"))
        contentType = "video/mp4"
      else
        contentType = "application/octet-stream"
      response.addHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(file.toPath.getFileName.toString, "UTF-8"))

      new FileInputStream(file)
    }.getOrElse(haltWithBody(404))
  }.getOrElse(haltWithBody(404))

  def encode = params.getAs[Long]("id").map {id =>
    Fileinfo.findById(id).map {fi =>
      val userAgent = request.getHeader("User-Agent")
      val encoder = """((Mobile/.+Safari)|Android)""".r findFirstIn userAgent match {
        case Some(_) => new HttpLiveStreamingEncoder(fi)
        case None => new StandardMP4Encoder(fi)
      }
      val output = encoder.encode()
      val relative = output.relativize(MovieEncoder.workDir)

      withFormat(Format.JSON) {
      s"""{"url" : "/videos/${relative.path}"}"""
      }
    }.getOrElse(haltWithBody(404))
  }.getOrElse(haltWithBody(404))

  val rootUrl = get("/")(showResources).as('root)
  val downloadUrl = get(s"/${resourcesName}/download/:id")(download).as('download)
  val encodeUrl = get(s"/${resourcesName}/encode/:id")(encode).as('encode)
}
