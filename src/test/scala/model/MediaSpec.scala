package model

import java.nio.file.Paths

import skinny.DBSettings
import skinny.test._
import org.scalatest.fixture.FunSpec
import org.scalatest._
import scalikejdbc._
import scalikejdbc.scalatest._
import org.joda.time._

class MediaSpec extends FunSpec with DBSettings with AutoRollback with DiagrammedAssertions {
  val sampleMovieDir = getClass.getResource("/sample_movies").getFile
  val sampleMovie = getClass.getResource("/sample_movies/mp4_h264_aac.mp4").getFile

  describe("createByTargetAndFile") {
    it("should create Media Record") { implicit session =>
      val target = FactoryGirl(Target).create('fullpath -> sampleMovieDir)
      val Some(id) = Media.createByTargetAndFile(target, Paths.get(sampleMovie).toFile)
      val Some(media) = Media.findById(id)
      assert(media.fullpath == sampleMovie)
      assert(media.relativePath == "mp4_h264_aac.mp4")
      assert(media.filesize == 5230167)
      assert(media.target.get == target)
    }
  }
}
