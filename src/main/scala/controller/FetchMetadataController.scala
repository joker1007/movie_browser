package controller

import akka.actor.{ActorRef, ActorSystem}
import skinny.controller.SkinnyController
import model.Fileinfo
import lib.FetchMetadataCommand

class FetchMetadataController(system: ActorSystem, actor: ActorRef) extends SkinnyController {
  def start = {
    Fileinfo.noMetadata.filter(_.fullpath.split("/").last.startsWith("(AV)")) foreach {fi =>
      actor ! (FetchMetadataCommand, fi)
    }

    redirect(url(FileinfosController.indexUrl))
  }
}
