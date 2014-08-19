package model

import java.nio.file.Paths

import org.apache.commons.io.FilenameUtils
import org.scalatest._
import scalikejdbc._
import scalikejdbc.scalatest._
import skinny._
import skinny.test.FactoryGirl

import scalaz.\/-

class ThumbnailGeneratorSpec extends fixture.FunSpec with DBSettings with AutoRollback with DiagrammedAssertions with BeforeAndAfter {
  val sampleMovieDir = getClass.getResource("/sample_movies").getFile
  val sampleMovie = getClass.getResource("/sample_movies/mp4_h264_aac.mp4").getFile

  before {
    Target.deleteAll()
    Media.deleteAll()
    Thumbnail.deleteAll()
  }

  def generator(implicit session: DBSession): ThumbnailGenerator = {
    val media = FactoryGirl(Media).create(
      'fullpath -> sampleMovie,
      'relativePath -> sampleMovie,
      'basename -> FilenameUtils.getBaseName(sampleMovie)
    )
    new ThumbnailGenerator(media)
  }

  describe("createMovieThumbnail") {
    it("create jpg file of movie thumbnail") { implicit session =>
      val g = generator
      val currentDir = Paths.get(".").toAbsolutePath
      val \/-(path) = g.createMovieThumbnail(currentDir)
    }
  }

  describe("createThumbnail") {
    it("create Thumbnail record") { implicit session =>
      val g = generator
      val \/-(id) = g.createThumbnail()
      val thumbnail = Thumbnail.findById(id)
      assert(thumbnail.isDefined)
      assert(thumbnail.get.data.nonEmpty)
    }
  }
}
