package controller

import org.scalatest._
import skinny._
import skinny.test._
import org.joda.time._
import model._

// NOTICE before/after filters won't be executed by default
class TargetsControllerSpec extends FunSpec with Matchers with BeforeAndAfterAll with DBSettings {

  override def afterAll() {
    super.afterAll()
    Target.deleteAll()
  }

  def createMockController = new TargetsController with MockController
  def newTarget = FactoryGirl(Target).create()

  describe("TargetsController") {

    describe("shows targets") {
      it("shows HTML response") {
        val controller = createMockController
        controller.showResources()
        controller.status should equal(200)
        controller.renderCall.map(_.path) should equal(Some("/targets/index"))
        controller.contentType should equal("text/html; charset=utf-8")
      }

      it("shows JSON response") {
        implicit val format = Format.JSON
        val controller = createMockController
        controller.showResources()
        controller.status should equal(200)
        controller.renderCall.map(_.path) should equal(Some("/targets/index"))
        controller.contentType should equal("application/json; charset=utf-8")
      }
    }

    describe("shows a target") {
      it("shows HTML response") {
        val target = newTarget
        val controller = createMockController
        controller.showResource(target.id)
        controller.status should equal(200)
        controller.getFromRequestScope[Target]("item") should equal(Some(target))
        controller.renderCall.map(_.path) should equal(Some("/targets/show"))
      }
    }

    describe("shows new resource input form") {
      it("shows HTML response") {
        val controller = createMockController
        controller.newResource()
        controller.status should equal(200)
        controller.renderCall.map(_.path) should equal(Some("/targets/new"))
      }
    }

    describe("creates a target") {
      it("succeeds with valid parameters") {
        val controller = createMockController
        controller.prepareParams(
          "fullpath" -> "dummy",
          "as_path" -> "dummy",
          "last_updated_at" -> skinny.util.DateTimeUtil.toString(new DateTime()))
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
      val target = newTarget
      val controller = createMockController
      controller.editResource(target.id)
      controller.status should equal(200)
        controller.renderCall.map(_.path) should equal(Some("/targets/edit"))
    }

    it("updates a target") {
      val target = newTarget
      val controller = createMockController
      controller.prepareParams(
        "fullpath" -> "dummy",
        "as_path" -> "dummy",
        "last_updated_at" -> skinny.util.DateTimeUtil.toString(new DateTime()))
      controller.updateResource(target.id)
      controller.status should equal(200)
    }

    it("destroys a target") {
      val target = newTarget
      val controller = createMockController
      controller.destroyResource(target.id)
      controller.status should equal(200)
    }

  }

}
