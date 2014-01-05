import skinny._
import skinny.controller._
import _root_.controller._
import java.util.Locale.ENGLISH

class ScalatraBootstrap extends SkinnyLifeCycle {

  object CustomAssetsController extends AssetsController with Routes {
    addMimeMapping("text/css", "css")

    get(s"${jsRootPath}/*")(js).as('js)
    get(s"${cssRootPath}/*")(css).as('css)
  }

  override def initSkinnyApp(ctx: ServletContext) {
    ThumbnailsController.mount(ctx)
    FileinfosController.mount(ctx)
    Controllers.root.mount(ctx)
    CustomAssetsController.mount(ctx)
  }

}

