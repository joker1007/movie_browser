package controller

import org.scalatra.test.scalatest._
import skinny._, test._
import org.joda.time._
import model._

class TargetsControllerSpec extends ScalatraFlatSpec with SkinnyTestSupport with DBSettings {
  addFilter(TargetsController, "/*")

  def target = FactoryGirl(Target).create()

  it should "show targets" in {
    get("/targets") {
      status should equal(200)
    }
    get("/targets/") {
      status should equal(200)
    }
    get("/targets.json") {
      status should equal(200)
    }
    get("/targets.xml") {
      status should equal(200)
    }
  }

  it should "show a target in detail" in {
    get(s"/targets/${target.id}") {
      status should equal(200)
    }
    get(s"/targets/${target.id}.xml") {
      status should equal(200)
    }
    get(s"/targets/${target.id}.json") {
      status should equal(200)
    }
  }

  it should "show new entry form" in {
    get(s"/targets/new") {
      status should equal(200)
    }
  }

  it should "create a target" in {
    post(s"/targets", "fullpath" -> "dummy") {
      status should equal(403)
    }

    withSession("csrf-token" -> "12345") {
      post(s"/targets", "fullpath" -> "/tmp", "csrf-token" -> "12345") {
        status should equal(302)
        val id = header("Location").split("/").last.toLong
        Target.findById(id).isDefined should equal(true)
      }
    }
  }

  it should "show the edit form" in {
    get(s"/targets/${target.id}/edit") {
      status should equal(200)
    }
  }

  it should "update a target" in {
    put(s"/targets/${target.id}", "fullpath" -> "dummy") {
      status should equal(403)
    }

    withSession("csrf-token" -> "12345") {
      put(s"/targets/${target.id}", "fullpath" -> "dummy", "csrf-token" -> "12345") {
        status should equal(302)
      }
    }
  }

  it should "delete a target" in {
    val target = FactoryGirl(Target).create()
    delete(s"/targets/${target.id}") {
      status should equal(403)
    }
    withSession("csrf-token" -> "aaaaaa") {
      delete(s"/targets/${target.id}?csrf-token=aaaaaa") {
        status should equal(200)
      }
    }
  }

}
