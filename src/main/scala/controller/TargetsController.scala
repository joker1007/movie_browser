package controller

import skinny._
import skinny.validator._
import model.Target

object TargetsController extends SkinnyResource {
  protectFromForgery()

  override def model = Target
  override def resourcesName = "targets"
  override def resourceName = "target"

  override def resourcesBasePath = s"/${toSnakeCase(resourcesName)}"
  override def viewsDirectoryPath = s"/${toSnakeCase(resourcesName)}"
  override def useSnakeCasedParamKeys = false

  override def createForm = validation(createParams,
    paramKey("fullpath") is required & maxLength(512),
    paramKey("asPath") is maxLength(512),
    paramKey("lastUpdatedAt") is dateTimeFormat
  )
  override def createParams = Params(params)
    .withDateTime("lastUpdatedAt")
  override def createFormStrongParameters = Seq(
    "fullpath" -> ParamType.String,
    "asPath" -> ParamType.String,
    "lastUpdatedAt" -> ParamType.DateTime
  )

  override def updateForm = validation(updateParams,
    paramKey("fullpath") is required & maxLength(512),
    paramKey("asPath") is maxLength(512),
    paramKey("lastUpdatedAt") is dateTimeFormat
  )
  override def updateParams = Params(params)
    .withDateTime("lastUpdatedAt")
  override def updateFormStrongParameters = Seq(
    "fullpath" -> ParamType.String,
    "asPath" -> ParamType.String,
    "lastUpdatedAt" -> ParamType.DateTime
  )

}
