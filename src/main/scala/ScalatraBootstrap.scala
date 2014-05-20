import akka.actor.{Props, ActorSystem}
import lib.BackendWorker
import skinny._
import skinny.controller._
import _root_.controller._


class ScalatraBootstrap extends SkinnyLifeCycle {
  // If you prefer more logging, configure this settings
  scalikejdbc.GlobalSettings.loggingSQLAndTime = scalikejdbc.LoggingSQLAndTimeSettings(
    singleLineMode = true
  )

  // simple worker example
  val sampleWorker = new skinny.worker.SkinnyWorker with Logging {
    def execute = logger.info("sample worker is called!")
  }

  override def initSkinnyApp(ctx: ServletContext) {
    Controllers.gathering.mount(ctx)
    Controllers.fetching.mount(ctx)
    TargetsController.mount(ctx)
    ThumbnailsController.mount(ctx)
    Controllers.fileinfos.mount(ctx)
    AssetsController.mount(ctx)
  }

}

