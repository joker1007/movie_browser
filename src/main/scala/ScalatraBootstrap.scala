import skinny._
import skinny.controller._
import _root_.controller._

class ScalatraBootstrap extends SkinnyLifeCycle {

  override def initSkinnyApp(ctx: ServletContext) {
    FileinfosController.mount(ctx)
    Controllers.root.mount(ctx)
    AssetsController.mount(ctx)
  }

}

