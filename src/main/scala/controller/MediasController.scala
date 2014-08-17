package controller

import skinny._
import skinny.validator._
import _root_.controller._
import model.Media

class MediasController extends SkinnyResource with ApplicationController {
  protectFromForgery()

  override def model = Media
  override def resourcesName = "medias"
  override def resourceName = "media"

  override def resourcesBasePath = s"/${toSnakeCase(resourcesName)}"
  override def useSnakeCasedParamKeys = true

  override def viewsDirectoryPath = s"/${resourcesName}"

  override def createParams = Params(params)
  override def createForm = validation(createParams,
    paramKey("md5") is required & maxLength(128),
    paramKey("fullpath") is required & maxLength(512),
    paramKey("relative_path") is required & maxLength(512),
    paramKey("basename") is required & maxLength(512),
    paramKey("filesize") is required & numeric & longValue,
    paramKey("target_id") is required & numeric & longValue
  )
  override def createFormStrongParameters = Seq(
    "md5" -> ParamType.String,
    "fullpath" -> ParamType.String,
    "relative_path" -> ParamType.String,
    "basename" -> ParamType.String,
    "filesize" -> ParamType.Long,
    "target_id" -> ParamType.Long
  )

  override def updateParams = Params(params)
  override def updateForm = validation(updateParams,
    paramKey("md5") is required & maxLength(128),
    paramKey("fullpath") is required & maxLength(512),
    paramKey("relative_path") is required & maxLength(512),
    paramKey("basename") is required & maxLength(512),
    paramKey("filesize") is required & numeric & longValue,
    paramKey("target_id") is required & numeric & longValue
  )
  override def updateFormStrongParameters = Seq(
    "md5" -> ParamType.String,
    "fullpath" -> ParamType.String,
    "relative_path" -> ParamType.String,
    "basename" -> ParamType.String,
    "filesize" -> ParamType.Long,
    "target_id" -> ParamType.Long
  )

}
