package model

import skinny.test._
import org.scalatest.fixture.FlatSpec
import scalikejdbc._, SQLInterpolation._
import scalikejdbc.scalatest._
import org.joda.time._
import org.scalatest.matchers.ShouldMatchers
import skinny.DBSettings
import scalax.file.Path

class FileinfoSpec extends FlatSpec with AutoRollback with DBSettings with ShouldMatchers {
  def fileinfo = Fileinfo(1, "hash", "/tmp/sample.mp4", 1, DateTime.now(), None)
  def testArchivePath = Fileinfo.getClass.getResource("/test_archive.zip")

  "Fileinfo.isMovie" should "return True if fullpath extension is mp4" in {implicit session =>
    fileinfo should be a 'movie
  }

  it should "return false if fullpath extension is zip" in {implicit session =>
    val archive = fileinfo.copy(fullpath = "/tmp/sample.zip")
    archive.isMovie should equal (false)
  }

  "Fileinfo.createZipThumbnail" should "create Thumbnail" in {implicit session =>
    val archive = fileinfo.copy(fullpath = testArchivePath.getPath)
    val tempDir = Path.createTempDirectory()
    archive.createZipThumbnail(tempDir)
  }
}
