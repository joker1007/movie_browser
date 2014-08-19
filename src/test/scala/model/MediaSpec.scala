package model

import java.nio.file.Paths

import skinny.DBSettings
import skinny.test._
import org.scalatest._
import scalikejdbc._
import scalikejdbc.scalatest._
import org.joda.time._
import org.scalacheck.Gen
import org.scalacheck.Prop._
import org.scalatest.prop.Checkers


class MediaSpec extends fixture.FunSpec with DBSettings with AutoRollback with DiagrammedAssertions with Checkers with BeforeAndAfter {
  val sampleMovieDir = getClass.getResource("/sample_movies").getFile
  val sampleMovie = getClass.getResource("/sample_movies/mp4_h264_aac.mp4").getFile

  before {
    Target.deleteAll()
    Media.deleteAll()
  }

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

  describe("isMovie") {
    val genExt = Gen.oneOf(Media.EXTENSIONS)
    def buildMedia(ext: String): Media =
      Media(1, "md5", s"/tmp/test.$ext", s"test.$ext", s"test.$ext", 100, 1, DateTime.now(), DateTime.now())

    describe("If extension is contained by MOVIE_EXTENSIONS") {
      it("should be true") { implicit session =>
        check(
          forAll(genExt) {(ext) =>
            Media.MOVIE_EXTENSIONS.contains(ext) ==> buildMedia(ext).isMovie ||
              !Media.MOVIE_EXTENSIONS.contains(ext) ==> !buildMedia(ext).isMovie
          }
        )
      }
    }
  }

  describe("isArchive") {
    val genExt = Gen.oneOf(Media.EXTENSIONS)
    def buildMedia(ext: String): Media =
      Media(1, "md5", s"/tmp/test.$ext", s"test.$ext", s"test.$ext", 100, 1, DateTime.now(), DateTime.now())

    describe("If extension is contained by MOVIE_EXTENSIONS") {
      it("should be true") { implicit session =>
        check(
          forAll(genExt) {(ext) =>
            Media.ARCHIVE_EXTENSIONS.contains(ext) ==> buildMedia(ext).isArchive ||
              !Media.ARCHIVE_EXTENSIONS.contains(ext) ==> !buildMedia(ext).isArchive
          }
        )
      }
    }
  }
}
