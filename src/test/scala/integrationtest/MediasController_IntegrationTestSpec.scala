package integrationtest

import org.scalatra.test.scalatest._
import org.scalatest._
import skinny._
import skinny.test._
import org.joda.time._
import _root_.controller.Controllers
import model._

class MediasController_IntegrationTestSpec extends ScalatraFlatSpec with SkinnyTestSupport with BeforeAndAfterAll with DBSettings {
  addFilter(Controllers.medias, "/*")

  override def afterAll() {
    super.afterAll()
    Media.deleteAll()
  }

  def newMedia = FactoryGirl(Media).create()

  it should "show medias" in {
    get("/medias") {
      logBodyUnless(200)
      status should equal(200)
    }
    get("/medias/") {
      logBodyUnless(200)
      status should equal(200)
    }
    get("/medias.json") {
      logBodyUnless(200)
      status should equal(200)
    }
    get("/medias.xml") {
      logBodyUnless(200)
      status should equal(200)
    }
  }

  it should "show a media in detail" in {
    get(s"/medias/${newMedia.id}") {
      logBodyUnless(200)
      status should equal(200)
    }
    get(s"/medias/${newMedia.id}.xml") {
      logBodyUnless(200)
      status should equal(200)
    }
    get(s"/medias/${newMedia.id}.json") {
      logBodyUnless(200)
      status should equal(200)
    }
  }

  it should "show new entry form" in {
    get(s"/medias/new") {
      logBodyUnless(200)
      status should equal(200)
    }
  }

  it should "create a media" in {
    post(s"/medias",
      "md5" -> "dummy",
      "fullpath" -> "dummy",
      "relative_path" -> "dummy",
      "basename" -> "dummy",
      "filesize" -> Long.MaxValue.toString(),
      "target_id" -> Long.MaxValue.toString()) {
      logBodyUnless(403)
      status should equal(403)
    }

    withSession("csrf-token" -> "valid_token") {
      post(s"/medias",
        "md5" -> "dummy",
        "fullpath" -> "dummy",
        "relative_path" -> "dummy",
        "basename" -> "dummy",
        "filesize" -> Long.MaxValue.toString(),
        "target_id" -> Long.MaxValue.toString(),
        "csrf-token" -> "valid_token") {
        logBodyUnless(302)
        status should equal(302)
        val id = header("Location").split("/").last.toLong
        Media.findById(id).isDefined should equal(true)
      }
    }
  }

  it should "show the edit form" in {
    get(s"/medias/${newMedia.id}/edit") {
      logBodyUnless(200)
      status should equal(200)
    }
  }

  it should "update a media" in {
    put(s"/medias/${newMedia.id}",
      "md5" -> "dummy",
      "fullpath" -> "dummy",
      "relative_path" -> "dummy",
      "basename" -> "dummy",
      "filesize" -> Long.MaxValue.toString(),
      "target_id" -> Long.MaxValue.toString()) {
      logBodyUnless(403)
      status should equal(403)
    }

    withSession("csrf-token" -> "valid_token") {
      put(s"/medias/${newMedia.id}",
        "md5" -> "dummy",
        "fullpath" -> "dummy",
        "relative_path" -> "dummy",
        "basename" -> "dummy",
        "filesize" -> Long.MaxValue.toString(),
        "target_id" -> Long.MaxValue.toString(),
        "csrf-token" -> "valid_token") {
        logBodyUnless(302)
        status should equal(302)
      }
    }
  }

  it should "delete a media" in {
    delete(s"/medias/${newMedia.id}") {
      logBodyUnless(403)
      status should equal(403)
    }
    withSession("csrf-token" -> "valid_token") {
      delete(s"/medias/${newMedia.id}?csrf-token=valid_token") {
        logBodyUnless(200)
        status should equal(200)
      }
    }
  }

}
