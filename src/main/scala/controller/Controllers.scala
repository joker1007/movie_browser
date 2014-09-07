package controller

import skinny._
import skinny.controller.AssetsController

object Controllers {

  def mount(ctx: ServletContext): Unit = {
    targets.mount(ctx)
    medias.mount(ctx)
    thumbnails.mount(ctx)
    root.mount(ctx)
    AssetsController.mount(ctx)
  }

  object root extends RootController with Routes {
    val indexUrl = get("/?")(index).as('index)
  }
  object medias extends _root_.controller.MediasController with Routes {
    val indexUrl = get("/medias")(index).as('index)
  }

  object targets extends _root_.controller.TargetsController with Routes {
  }

  object thumbnails extends _root_.controller.ThumbnailsController with Routes {
    val showUrl = get("/thumbnails/:hash.jpg")(show).as('show)
  }

}
