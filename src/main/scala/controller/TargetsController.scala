package controller

import skinny._
import skinny.validator._
import model.Target
import lib.FileGather
import scalax.file.Path

object TargetsController extends SkinnyResource {
  protectFromForgery()
  override def scalateExtension = "scaml"
  override def model = Target
  override def resourcesName = "targets"
  override def resourceName = "target"

  override def createForm = validation(createParams,
    paramKey("fullpath") is required & maxLength(512),
    paramKey("lastUpdatedAt") is dateTimeFormat
  )
  override def createParams = Params(params)
    .withDateTime("lastUpdatedAt")
  override def createFormStrongParameters = Seq(
    "fullpath" -> ParamType.String,
    "lastUpdatedAt" -> ParamType.DateTime
  )

  override def updateForm = validation(updateParams,
    paramKey("fullpath") is required & maxLength(512),
    paramKey("lastUpdatedAt") is dateTimeFormat
  )
  override def updateParams = Params(params)
    .withDateTime("lastUpdatedAt")
  override def updateFormStrongParameters = Seq(
    "fullpath" -> ParamType.String,
    "lastUpdatedAt" -> ParamType.DateTime
  )

  def gather = params.getAs[Long]("id").map { id =>
    Target.findById(id).map { target =>
      FileGather.gather(Path(target.fullpath))
      redirect(redirect(s"/${resourcesName}"))
    } getOrElse haltWithBody(404)
  } getOrElse haltWithBody(404)


}
