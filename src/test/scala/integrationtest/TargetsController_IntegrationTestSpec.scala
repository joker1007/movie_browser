package integrationtest

import org.scalatra.test.scalatest._
import org.scalatest._
import skinny._
import skinny.test._
import org.joda.time._
import _root_.controller.Controllers
import model._

class TargetsController_IntegrationTestSpec extends ScalatraFlatSpec with SkinnyTestSupport with BeforeAndAfterAll with DBSettings {
  addFilter(Controllers.targets, "/*")

  override def afterAll() {
    super.afterAll()
    Target.deleteAll()
  }

  def newTarget = FactoryGirl(Target).create()

  it should "show targets" in {
    get("/targets") {
      logBodyUnless(200)
      status should equal(200)
    }
    get("/targets/") {
      logBodyUnless(200)
      status should equal(200)
    }
    get("/targets.json") {
      logBodyUnless(200)
      status should equal(200)
    }
    get("/targets.xml") {
      logBodyUnless(200)
      status should equal(200)
    }
  }

  it should "show a target in detail" in {
    get(s"/targets/${newTarget.id}") {
      logBodyUnless(200)
      status should equal(200)
    }
    get(s"/targets/${newTarget.id}.xml") {
      logBodyUnless(200)
      status should equal(200)
    }
    get(s"/targets/${newTarget.id}.json") {
      logBodyUnless(200)
      status should equal(200)
    }
  }

  it should "show new entry form" in {
    get(s"/targets/new") {
      logBodyUnless(200)
      status should equal(200)
    }
  }

  it should "create a target" in {
    post(s"/targets",
      "fullpath" -> "dummy",
      "as_path" -> "dummy",
      "last_updated_at" -> skinny.util.DateTimeUtil.toString(new DateTime())) {
      logBodyUnless(403)
      status should equal(403)
    }

    withSession("csrf-token" -> "valid_token") {
      post(s"/targets",
        "fullpath" -> "dummy",
        "as_path" -> "dummy",
        "last_updated_at" -> skinny.util.DateTimeUtil.toString(new DateTime()),
        "csrf-token" -> "valid_token") {
        logBodyUnless(302)
        status should equal(302)
        val id = header("Location").split("/").last.toLong
        Target.findById(id).isDefined should equal(true)
      }
    }
  }

  it should "show the edit form" in {
    get(s"/targets/${newTarget.id}/edit") {
      logBodyUnless(200)
      status should equal(200)
    }
  }

  it should "update a target" in {
    put(s"/targets/${newTarget.id}",
      "fullpath" -> "dummy",
      "as_path" -> "dummy",
      "last_updated_at" -> skinny.util.DateTimeUtil.toString(new DateTime())) {
      logBodyUnless(403)
      status should equal(403)
    }

    withSession("csrf-token" -> "valid_token") {
      put(s"/targets/${newTarget.id}",
        "fullpath" -> "dummy",
        "as_path" -> "dummy",
        "last_updated_at" -> skinny.util.DateTimeUtil.toString(new DateTime()),
        "csrf-token" -> "valid_token") {
        logBodyUnless(302)
        status should equal(302)
      }
    }
  }

  it should "delete a target" in {
    delete(s"/targets/${newTarget.id}") {
      logBodyUnless(403)
      status should equal(403)
    }
    withSession("csrf-token" -> "valid_token") {
      delete(s"/targets/${newTarget.id}?csrf-token=valid_token") {
        logBodyUnless(200)
        status should equal(200)
      }
    }
  }

}
