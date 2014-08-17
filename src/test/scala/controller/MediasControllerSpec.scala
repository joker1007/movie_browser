package controller

import org.scalatest._
import skinny._
import skinny.test._
import org.joda.time._
import model._

// NOTICE before/after filters won't be executed by default
class MediasControllerSpec extends FunSpec with Matchers with BeforeAndAfterAll with DBSettings {

  override def afterAll() {
    super.afterAll()
    Media.deleteAll()
  }

  def createMockController = new MediasController with MockController
  def newMedia = FactoryGirl(Media).create()

  describe("MediasController") {

    describe("shows medias") {
      it("shows HTML response") {
        val controller = createMockController
        controller.showResources()
        controller.status should equal(200)
        controller.renderCall.map(_.path) should equal(Some("/medias/index"))
        controller.contentType should equal("text/html; charset=utf-8")
      }

      it("shows JSON response") {
        implicit val format = Format.JSON
        val controller = createMockController
        controller.showResources()
        controller.status should equal(200)
        controller.renderCall.map(_.path) should equal(Some("/medias/index"))
        controller.contentType should equal("application/json; charset=utf-8")
      }
    }

    describe("shows a media") {
      it("shows HTML response") {
        val media = newMedia
        val controller = createMockController
        controller.showResource(media.id)
        controller.status should equal(200)
        controller.getFromRequestScope[Media]("item") should equal(Some(media))
        controller.renderCall.map(_.path) should equal(Some("/medias/show"))
      }
    }

    describe("shows new resource input form") {
      it("shows HTML response") {
        val controller = createMockController
        controller.newResource()
        controller.status should equal(200)
        controller.renderCall.map(_.path) should equal(Some("/medias/new"))
      }
    }

    describe("creates a media") {
      it("succeeds with valid parameters") {
        val controller = createMockController
        controller.prepareParams(
          "md5" -> "dummy",
          "fullpath" -> "dummy",
          "relative_path" -> "dummy",
          "basename" -> "dummy",
          "filesize" -> Long.MaxValue.toString(),
          "target_id" -> Long.MaxValue.toString())
        controller.createResource()
        controller.status should equal(200)
      }

      it("fails with invalid parameters") {
        val controller = createMockController
        controller.prepareParams() // no parameters
        controller.createResource()
        controller.status should equal(400)
        controller.errorMessages.size should be >(0)
      }
    }

    it("shows a resource edit input form") {
      val media = newMedia
      val controller = createMockController
      controller.editResource(media.id)
      controller.status should equal(200)
        controller.renderCall.map(_.path) should equal(Some("/medias/edit"))
    }

    it("updates a media") {
      val media = newMedia
      val controller = createMockController
      controller.prepareParams(
        "md5" -> "dummy",
        "fullpath" -> "dummy",
        "relative_path" -> "dummy",
        "basename" -> "dummy",
        "filesize" -> Long.MaxValue.toString(),
        "target_id" -> Long.MaxValue.toString())
      controller.updateResource(media.id)
      controller.status should equal(200)
    }

    it("destroys a media") {
      val media = newMedia
      val controller = createMockController
      controller.destroyResource(media.id)
      controller.status should equal(200)
    }

  }

}
