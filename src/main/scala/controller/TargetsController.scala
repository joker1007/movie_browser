package controller

import skinny._
import skinny.validator._
import _root_.controller._
import model.Target

class TargetsController extends SkinnyResource with ApplicationController {
  protectFromForgery()

  override def model = Target
  override def resourcesName = "targets"
  override def resourceName = "target"

  override def resourcesBasePath = s"/${toSnakeCase(resourcesName)}"
  override def useSnakeCasedParamKeys = true

  override def viewsDirectoryPath = s"/${resourcesName}"

  override def createParams = Params(params).withDateTime("last_updated_at")
  override def createForm = validation(createParams,
    paramKey("fullpath") is required & maxLength(512),
    paramKey("as_path") is maxLength(512),
    paramKey("last_updated_at") is dateTimeFormat
  )
  override def createFormStrongParameters = Seq(
    "fullpath" -> ParamType.String,
    "as_path" -> ParamType.String,
    "last_updated_at" -> ParamType.DateTime
  )

  override def updateParams = Params(params).withDateTime("last_updated_at")
  override def updateForm = validation(updateParams,
    paramKey("fullpath") is required & maxLength(512),
    paramKey("as_path") is maxLength(512),
    paramKey("last_updated_at") is dateTimeFormat
  )
  override def updateFormStrongParameters = Seq(
    "fullpath" -> ParamType.String,
    "as_path" -> ParamType.String,
    "last_updated_at" -> ParamType.DateTime
  )

}
