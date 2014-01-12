package model

import scalikejdbc._, SQLInterpolation._
import org.joda.time.DateTime
import scalikejdbc.scalatest.AutoRollback
import org.scalatest.fixture.FunSpec
import org.scalatest.matchers.ShouldMatchers
import skinny.DBSettings
import skinny.test.FactoryGirl


class FileMetadataSpec extends FunSpec with AutoRollback with DBSettings with ShouldMatchers {
  def fileMetadata = FactoryGirl(FileMetadata).create()

  override def fixture(implicit session: DBSession) {
    val fm = FactoryGirl(FileMetadata).create('md5 -> "md5")
    val itemInfo = FactoryGirl(ItemInfo).create()
    val itemInfo2 = FactoryGirl(ItemInfo).create('dmmId -> "dmmId2", 'name -> "keywordName2")
    MetadataItemInfo.createWithAttributes('fileMetadataId -> fm.id, 'itemInfoId -> itemInfo.id)
    MetadataItemInfo.createWithAttributes('fileMetadataId -> fm.id, 'itemInfoId -> itemInfo2.id)
  }

  describe("Relation") {
    it("should has itemInfos relation") {implicit session =>
      val fms = FileMetadata.findAll()
      fms.head.itemInfos.size should equal (2)
    }
  }
}
