package controller

import skinny._
import skinny.validator._
import model._
import scalikejdbc._, SQLInterpolation._
import java.io.FileInputStream
import java.net.URLEncoder
import scala.collection.JavaConversions._
import akka.actor.ActorSystem
import org.scalatra.{FutureSupport}
import scala.concurrent.{ExecutionContext}
import scala.Some
import skinny.validator.maxLength
import org.slf4j.LoggerFactory
import scalax.file.PathMatcher.IsFile
import scalax.file.Path

class FileinfosController(system: ActorSystem) extends SkinnyResource with FutureSupport {
  protectFromForgery()
  override def scalateExtension = "scaml"
  override def model = Fileinfo
  override def resourcesName = "fileinfos"
  override def resourceName = "fileinfo"

  addMimeMapping("video/mp4", "mp4")
  addMimeMapping("video/webm", "webm")
  addMimeMapping("application/octet-stream", "avi")
  addMimeMapping("image/jpeg", "jpg")
  addMimeMapping("image/png", "png")

  protected implicit def executor: ExecutionContext = system.dispatcher

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

  override def showResource(id: Long)(implicit format: Format = Format.HTML): Any = withFormat(format) {
    val fileinfo = model.findModel(id).getOrElse(haltWithBody(404))
    set(resourceName, fileinfo)
    if (fileinfo.isArchive()) {
      templateAttributes += "layout" -> "WEB-INF/layouts/simple.jade"
      render(s"/${resourcesName}/archive_show")
    } else
      render(s"/${resourcesName}/show")
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

  def extract = params.getAs[Long]("id").map {id =>
    Fileinfo.findById(id).map {fileinfo =>
      params.getAs[Int]("page").map {page =>
        val ePath = new ArchiveExtractor(fileinfo).extract()
        ePath match {
          case Left(th) =>
            LoggerFactory.getLogger(getClass).error("Extract Failed", th)
            Array.emptyByteArray
          case Right(path: Path) =>
            contentType = "image/jpeg"
            val children = path.children(IsFile).toArray
            if (children.size < page)
              haltWithBody(404)
            val file = children.sortBy(_.path).apply(page - 1)
            file.extension.map {
              case "jpg" => contentType = "image/jpeg"
              case "png" => contentType = "image/png"
            }
            file.byteArray
        }
      }.getOrElse(haltWithBody(404))
    }.getOrElse(haltWithBody(404))
  }.getOrElse(haltWithBody(404))

  def rename = params.getAs[Long]("id").map {id =>
    Fileinfo.findById(id).map {fileinfo =>
      fileinfo.renameWithMetadata("(AV)") match {
        case Some(f) =>
          flash += "notice" -> s"Rename to ${f.basename}"
        case None =>
          flash += "notice" -> s"No metadata"
      }
      redirect("/")
    }.getOrElse(haltWithBody(404))
  }.getOrElse(haltWithBody(404))

  val rootUrl = get("/")(showResources).as('root)
  val downloadUrl = get(s"/${resourcesName}/download/:id")(download).as('download)
  val encodeUrl = get(s"/${resourcesName}/encode/:id")(encode).as('encode)
  val extractUrl = get(s"/${resourcesName}/extract/:id/:page")(extract).as('extract)
  val renameUrl = get(s"/${resourcesName}/rename/:id")(rename).as('rename)
}
