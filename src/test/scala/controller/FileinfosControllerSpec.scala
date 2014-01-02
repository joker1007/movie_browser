package controller

import org.scalatra.test.scalatest._
import skinny._, test._
import org.joda.time._
import model._
import org.scalatest.BeforeAndAfter
import scalikejdbc._, SQLInterpolation._

class FileinfosControllerSpec extends ScalatraFlatSpec with SkinnyTestSupport with DBSettings with BeforeAndAfter {
  addFilter(FileinfosController, "/*")

  def fileinfo = FactoryGirl(Fileinfo).create()

  before {
    Fileinfo.deleteBy(sqls"true = true")
  }

  it should "show fileinfos" in {
    get("/fileinfos") {
      status should equal(200)
    }
    get("/fileinfos/") {
      status should equal(200)
    }
    get("/fileinfos.json") {
      status should equal(200)
    }
    get("/fileinfos.xml") {
      status should equal(200)
    }
  }

  it should "show a fileinfo in detail" in {
    val fileinfo = FactoryGirl(Fileinfo).create()
    get(s"/fileinfos/${fileinfo.id}") {
      status should equal(200)
    }
    get(s"/fileinfos/${fileinfo.id}.xml") {
      status should equal(200)
    }
    get(s"/fileinfos/${fileinfo.id}.json") {
      status should equal(200)
    }
  }

  it should "show new entry form" in {
    get(s"/fileinfos/new") {
      status should equal(200)
    }
  }

  it should "create a fileinfo" in {
    post(s"/fileinfos", "md5" -> "dummy","fullpath" -> "dummy","filesize" -> "dummy") {
      status should equal(403)
    }

    withSession("csrf-token" -> "12345") {
      post(s"/fileinfos", "md5" -> "dummy","fullpath" -> "dummy","filesize" -> "1234", "csrf-token" -> "12345") {
        status should equal(302)
        val id = header("Location").split("/").last.toLong
        Fileinfo.findById(id).isDefined should equal(true)
      }
    }
  }

  it should "show the edit form" in {
    get(s"/fileinfos/${fileinfo.id}/edit") {
      status should equal(200)
    }
  }

  it should "update a fileinfo" in {
    val fileinfo = FactoryGirl(Fileinfo).create()
    put(s"/fileinfos/${fileinfo.id}", "md5" -> "dummy","fullpath" -> "dummy","filesize" -> "dummy") {
      status should equal(403)
    }

    withSession("csrf-token" -> "12345") {
      put(s"/fileinfos/${fileinfo.id}", "md5" -> "dummy","fullpath" -> "dummy","filesize" -> "1234", "csrf-token" -> "12345") {
        status should equal(200)
      }
    }
  }

  it should "delete a fileinfo" in {
    val fileinfo = FactoryGirl(Fileinfo).create()
    delete(s"/fileinfos/${fileinfo.id}") {
      status should equal(403)
    }
    withSession("csrf-token" -> "aaaaaa") {
      delete(s"/fileinfos/${fileinfo.id}?csrf-token=aaaaaa") {
        status should equal(200)
      }
    }
  }

}
