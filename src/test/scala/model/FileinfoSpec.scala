package model

import skinny.test._
import org.scalatest.fixture.FunSpec
import scalikejdbc._, SQLInterpolation._
import scalikejdbc.scalatest._
import org.joda.time._
import org.scalatest.matchers.ShouldMatchers
import skinny.DBSettings
import scalax.file.Path

class FileinfoSpec extends FunSpec with AutoRollback with DBSettings with ShouldMatchers {
  def fileinfo = Fileinfo(
    id = 1,
    md5 = "hash",
    fullpath = "/tmp/sample.mp4",
    relativePath = "sample.mp4",
    filesize = 1,
    basename = "sample.mp4",
    createdAt = DateTime.now(),
    updatedAt = None,
    targetId = 1
  )
  def testArchivePath = Fileinfo.getClass.getResource("/test_archive.zip")

  override def fixture(implicit session: DBSession) {
    FactoryGirl(Fileinfo).create('md5 -> "hash1")
    val fm = FactoryGirl(FileMetadata).create()
    val itemInfo = FactoryGirl(ItemInfo).create()
    val itemInfo2 = FactoryGirl(ItemInfo).create('dmmId -> "dmmId2", 'name -> "keywordName2")
    MetadataItemInfo.createWithAttributes('fileMetadataId -> fm.id, 'itemInfoId -> itemInfo.id)
    MetadataItemInfo.createWithAttributes('fileMetadataId -> fm.id, 'itemInfoId -> itemInfo2.id)
  }

  describe("isMovie") {
    it("should return True if fullpath extension is mp4") {implicit session =>
      fileinfo should be a 'movie
    }

    it("should return false if fullpath extension is zip") {implicit session =>
      val archive = fileinfo.copy(fullpath = "/tmp/sample.zip")
      archive.isMovie should equal (false)
    }
  }

  describe("createZipThumbnail") {
    it("create Thumbnail record") {implicit session =>
      val archive = fileinfo.copy(fullpath = testArchivePath.getPath)
      val tempDir = Path.createTempDirectory()
      new ThumbnailGenerator(archive).createZipThumbnail(tempDir)
    }
  }

  describe("joinAllByPaging") {
    it("should select joined table with FileMetadata, MetadataItemInfo, ItemInfo") {implicit session =>
      val fileinfos = Fileinfo.joinAllByPaging()
      fileinfos.size should equal (1)
      fileinfos.head.fileMetadata should not be (None)
    }
  }
}
