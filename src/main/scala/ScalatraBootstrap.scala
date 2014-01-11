import akka.actor.{Props, ActorSystem}
import lib.FileGatherActor
import skinny._
import skinny.controller._
import _root_.controller._


class ScalatraBootstrap extends SkinnyLifeCycle {

  val actorSystem = ActorSystem()
  val gatherActor = actorSystem.actorOf(Props[FileGatherActor])

  override def initSkinnyApp(ctx: ServletContext) {
    object gathering extends GatheringController(actorSystem, gatherActor) with Routes {
      val startUrl = post("/gathering")(start).as('start)
    }
    gathering.mount(ctx)
    TargetsController.mount(ctx)
    ThumbnailsController.mount(ctx)
    FileinfosController.mount(ctx)
    AssetsController.mount(ctx)
  }

}

