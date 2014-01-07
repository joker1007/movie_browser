package controller

import skinny._
import skinny.validator._
import model.Target
import lib.FileGather
import akka.actor.{ActorSystem, ActorRef}
import org.scalatra.Accepted


class GatheringController(system: ActorSystem, actor: ActorRef) extends SkinnyController {
  def start = params.getAs[Long]("id") map {id =>
    println(id)
    Target.findById(id) map {target =>
      actor ! target
      flash += "notice" -> "Gathering"
      redirect(url(TargetsController.indexUrl))
    } getOrElse haltWithBody(404)
  } getOrElse haltWithBody(404)
}