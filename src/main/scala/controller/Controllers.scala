package controller

import skinny._
import skinny.controller.AssetsController

object Controllers {

  def mount(ctx: ServletContext): Unit = {
    targets.mount(ctx)
    medias.mount(ctx)
    root.mount(ctx)
    AssetsController.mount(ctx)
  }

  object root extends RootController with Routes {
    val indexUrl = get("/?")(index).as('index)
  }
  object medias extends _root_.controller.MediasController with Routes {
  }

  object targets extends _root_.controller.TargetsController with Routes {
  }

}
