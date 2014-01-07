package controller

import skinny._
import skinny.validator._
import model.Fileinfo
import scalikejdbc._, SQLInterpolation._

object FileinfosController extends SkinnyResource {
  protectFromForgery()
  override def scalateExtension = "scaml"
  override def model = Fileinfo
  override def resourcesName = "fileinfos"
  override def resourceName = "fileinfo"

  override def showResources()(implicit format: Format = Format.HTML): Any = withFormat(format) {
    val query = params.getAs[String]("q")
    val page = params.getAs[Int]("page") getOrElse 1
    println(page)
    val f = Fileinfo.defaultAlias
    query match {
      case Some(q) =>
        val resources = model.searchByPath(q)
        set(resourcesName, resources)
      case None =>
        set(resourcesName, model.findAllByPaging(sqls"1 = 1", Fileinfo.PER_PAGE, (page - 1) * Fileinfo.PER_PAGE, sqls"${f.fullpath} asc"))
    }
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

  val rootUrl = get("/")(showResources).as('root)
}
