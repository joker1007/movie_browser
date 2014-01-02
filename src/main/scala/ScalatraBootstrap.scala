import skinny._
import skinny.controller._
import _root_.controller._

class ScalatraBootstrap extends SkinnyLifeCycle {

  override def initSkinnyApp(ctx: ServletContext) {
    ThumbnailsController.mount(ctx)
    FileinfosController.mount(ctx)
    Controllers.root.mount(ctx)
    AssetsController.mount(ctx)
  }

}

