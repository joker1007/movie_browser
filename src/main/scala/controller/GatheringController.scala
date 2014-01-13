package controller

import skinny._
import model.Target
import lib.GatherCommand
import akka.actor.{ActorSystem, ActorRef}


class GatheringController(system: ActorSystem, actor: ActorRef) extends SkinnyController {
  def start = params.getAs[Long]("id") map {id =>
    println(id)
    Target.findById(id) map {target =>
      actor ! (GatherCommand, target)
      flash += "notice" -> "Gathering"
      redirect(url(TargetsController.indexUrl))
    } getOrElse haltWithBody(404)
  } getOrElse haltWithBody(404)
}