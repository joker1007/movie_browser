package controller

import skinny._
import akka.actor.{Props, ActorSystem}
import lib.BackendWorker

object Controllers {
  val actorSystem = ActorSystem()
  val gatherActor = actorSystem.actorOf(Props[BackendWorker])

  object gathering extends GatheringController(actorSystem, gatherActor) with Routes {
    val startUrl = post("/gathering")(start).as('start)
  }
  object fetching extends FetchMetadataController(actorSystem, gatherActor) with Routes {
    val startUrl = post("/fetching")(start).as('start)
  }

  object fileinfos extends FileinfosController(actorSystem)
}

