package model

import skinny.orm._, feature._
import scalikejdbc._, SQLInterpolation._
import org.joda.time._
import scalax.file.Path
import scalax.io.Resource
import java.security.MessageDigest
import scala.sys.process._
import scala.collection.mutable.ArrayBuffer
import java.util.zip.{ZipInputStream}
import java.io.{File => JFile, FileInputStream, FileOutputStream, BufferedOutputStream}
import scala.util.control.Exception._
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import model.dmm.{DmmItemInfo, DmmItem, ApiClient}
import org.slf4j.LoggerFactory

object IsMovie {
  def unapply(f: Fileinfo): Option[Fileinfo] = {
    if (f.isMovie)
      Some(f)
    else
      None
  }
}

object IsArchive {
  def unapply(f: Fileinfo): Option[Fileinfo] = {
    if (f.isArchive)
      Some(f)
    else
      None
  }
}

object IsZip {
  def unapply(f: Fileinfo): Option[Fileinfo] = {
    if (f.extension == "zip")
      Some(f)
    else
      None
  }
}

case class Fileinfo(
  id: Long,
  md5: String,
  fullpath: String,
  filesize: Long,
  createdAt: DateTime,
  updatedAt: Option[DateTime] = None,
  fileMetadata: Option[FileMetadata] = None
) {
  def isMovie(): Boolean = Fileinfo.MOVIE_EXTENSIONS.contains(extension)
  def isArchive(): Boolean = Fileinfo.ARCHIVE_EXTENSIONS.contains(extension)
  def extension: String = Path(fullpath, '/').extension.getOrElse("")

  def normalizeBasename: String = {
    Path(fullpath, '/').simpleName
      .replaceAll("""\.(ogm|avi)""", "")
      .replaceAll("""^\(.*?\)( ?\[.*?\])? ?""", "") // Remove prefix
      .replaceAll("""(\(.*?\)|\[.*?\]) *$""", "") // Remove suffix
      .replaceAll(""" +- +""", " ")
      .replaceAll("""・""", " ")
      .trim
  }

  def fetchDMM(site: String = "DMM.co.jp"): Option[DmmItem] = {
    ApiClient.fetch(Map("site" -> site, "keyword" -> normalizeBasename)).headOption
  }

  def createMetadataFromDMM(site: String = "DMM.co.jp") {
    fetchDMM(site) match {
      case Some(item) =>
        val fm = FileMetadata.defaultAlias
        val fileMetadataId = FileMetadata.findBy(sqls"${fm.md5} = $md5") match {
          case Some(fileMetadata) => fileMetadata.id
          case None => createMetadataWithAttributes(
            'title -> item.title,
            'url -> item.url,
            'imageUrl -> item.imageUrl,
            'largeImageUrl -> item.largeImageUrl
          )
        }

        item.itemInfo foreach (createTagWithItemInfo(_, fileMetadataId))
      case None =>
        val logger = LoggerFactory.getLogger(getClass())
        logger.info(s"Not found item information for '$normalizeBasename'")
    }
  }

  def createMetadataWithAttributes(attributes: (Symbol, Any)*): Long = {
    val attributesWithMd5 = attributes.+:('md5, md5)
    FileMetadata.createWithAttributes(attributesWithMd5: _*)
  }

  def createTagWithItemInfo(info: DmmItemInfo, fileMetadataId: Long) {
    val ii = ItemInfo.defaultAlias
    try {
      ItemInfo.findBy(sqls.eq(ii.dmmId, info.id)) match {
        case Some(itemInfo) =>
          MetadataItemInfo.createWithAttributes('fileMetadataId -> fileMetadataId, 'itemInfoId -> itemInfo.id)
        case None =>
          val itemInfoId = ItemInfo.createWithAttributes('dmmId -> info.id, 'kind -> info.infoType, 'name -> info.name)
          MetadataItemInfo.createWithAttributes('fileMetadataId -> fileMetadataId, 'itemInfoId -> itemInfoId)
      }
    } catch {
      case e: org.h2.jdbc.JdbcSQLException =>
    }
  }

  def createThumbnail(percentage: Int = 10, width: Int = 240, count:Int = 4, force: Boolean = false) {
    val t = Thumbnail.defaultAlias
    val oThumbnail = Thumbnail.findBy(sqls.eq(t.md5, md5))

    oThumbnail match {
      case Some(thumb) =>
        if (force)
          Thumbnail.deleteById(thumb.id)
        else
          return
      case None =>
    }

    val tempDir = Path.createTempDirectory()
    this match {
      case IsMovie(f) =>
        createMovieThumbnail(tempDir, percentage, width, count) match {
          case Left(throwable) => print("Error: "); println(throwable); throwable.printStackTrace()
          case Right(merged) =>
            val data = merged.byteArray
            createThumbnailModel(data)
        }
      case IsZip(f) =>
        createZipThumbnail(tempDir, width, count) match {
          case Left(throwable) => print("Error: "); println(throwable); throwable.printStackTrace()
          case Right(merged) =>
            val data = merged.byteArray
            createThumbnailModel(data)
        }
      case _ =>
    }
  }

  def createMovieThumbnail(tempDir: Path, percentage: Int = 10, width: Int = 240, count:Int = 4): Either[Throwable, Path] = {
    val convertCmd: ArrayBuffer[String] = ArrayBuffer("convert", "+append")
    allCatch either {
      for (i <- 1 to count) {
        val output = tempDir / s"${md5}_$i.jpg"
        Seq("ffmpegthumbnailer", "-s", s"$width", "-t", s"${percentage * i}%", "-i", fullpath, "-o", output.path).!!
        convertCmd += output.path
      }
      val merged = tempDir / s"$md5.jpg"
      convertCmd += merged.path
      convertCmd.!!
      merged
    }
  }

  def createZipThumbnail(tempDir: Path, width: Int = 240, count:Int = 4): Either[Throwable,Path] = {
    val file = new JFile(fullpath)
    val zis = new ZipArchiveInputStream(new FileInputStream(file), "Windows-31J")
    allCatch either {
      using(zis) {zs =>
        val iter = Iterator.continually(zs.getNextZipEntry()).filterNot(_.isDirectory()).filter {e =>
          Fileinfo.IMAGE_EXTENSIONS.contains(Path(e.getName(), '/').extension.getOrElse(""))
        }.filterNot {e =>
          val name = e.getName()
          val invalidPattern = """((^\.)|(__MACOSX)|(DS_Store))""".r
          invalidPattern.findFirstIn(name).isDefined
        }.take(count).zipWithIndex

        val convertCmd: ArrayBuffer[String] = ArrayBuffer("convert", "+append")
        for ((e, idx) <- iter) {
          val ext = Path(e.getName, '/').extension.get
          val output = tempDir / s"${md5}_$idx.$ext"
          val fos = new BufferedOutputStream(new FileOutputStream(new JFile(output.path)))
          using(fos) {os =>
            Iterator.continually(zs.read()).takeWhile(_ != -1).foreach(os write _)
          }
          convertCmd += output.path
          val mogrifyCmd: List[String] = List("mogrify", "-resize", s"${width}x", output.path)
          mogrifyCmd.!!
          println(output.path)
        }
        val merged = tempDir / s"$md5.jpg"
        convertCmd += merged.path
        convertCmd.!!
        merged
      }
    }
  }

  private[this] def createThumbnailModel(data: Array[Byte]) = Thumbnail.createWithAttributes(
    'md5 -> md5,
    'data -> data
  )
}

object Fileinfo extends SkinnyCRUDMapper[Fileinfo] with TimestampsFeature[Fileinfo] {
  override val tableName = "fileinfos"
  override val defaultAlias = createAlias("f")

  val PER_PAGE = 200
  val READ_LIMIT = 1024 * 1024 * 3 // 3MB
  val MOVIE_EXTENSIONS = Array("mp4", "m4v", "mpg", "avi", "wmv", "ogm", "ogg", "asf")
  val ARCHIVE_EXTENSIONS = Array("zip", "rar")
  val IMAGE_EXTENSIONS = Array("jpg", "jpeg", "png")
  val EXTENSIONS = MOVIE_EXTENSIONS ++ ARCHIVE_EXTENSIONS

  override def extract(rs: WrappedResultSet, rn: ResultName[Fileinfo]): Fileinfo = new Fileinfo(
    id = rs.long(rn.id),
    md5 = rs.string(rn.md5),
    fullpath = rs.string(rn.fullpath),
    filesize = rs.long(rn.filesize),
    createdAt = rs.dateTime(rn.createdAt),
    updatedAt = rs.dateTimeOpt(rn.updatedAt)
  )

  def createFromFile(file: Path, force: Boolean = false): Option[Long] = {
    if (!file.canRead)
      return None

    if (!EXTENSIONS.contains(file.extension.getOrElse("")))
      return None

    val f = defaultAlias

    if (isExistByFullpath(file.path) && !force)
      return None

    val sum = getMd5FromFile(file)

    findBy(sqls.eq(f.md5, sum)) match {
      case Some(fileinfo) =>
        fileinfo.createThumbnail()
        return None
      case _ =>
    }

    val fileinfoId = createWithAttributes(
      'md5 -> sum,
      'fullpath -> file.path,
      'filesize -> file.size
    )

    val fileinfo = findById(fileinfoId).get
    fileinfo.createThumbnail()

    Some(fileinfoId)
  }

  private[this] def getMd5FromFile(file: Path): String = {
    val bytes = file.bytes.take(READ_LIMIT).toArray
    val md5 = MessageDigest.getInstance("MD5")
    md5.reset()
    md5.update(bytes)
    md5.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}
  }

  def isExistByFullpath(fullpath: String): Boolean = {
    val f = defaultAlias
    countBy(sqls.eq(f.fullpath, fullpath)) > 0
  }

  def searchByPath(pathString: String, page: Int = 1): List[Fileinfo] = {
    findAllByPaging(sqls.like(defaultAlias.fullpath, s"%$pathString%"), PER_PAGE, (page - 1) * PER_PAGE, sqls"${defaultAlias.fullpath} asc")
  }

  def searchByPathWithTotalCount(pathString: String, page: Int = 1): (List[Fileinfo], Long) = {
    val count = countBy(sqls.like(defaultAlias.fullpath, s"%$pathString%"))
    (findAllByPaging(sqls.like(defaultAlias.fullpath, s"%$pathString%"), PER_PAGE, (page - 1) * PER_PAGE, sqls"${defaultAlias.fullpath} asc"), count)
  }

  def joinTableSyntaxes = (Fileinfo.syntax, FileMetadata.syntax, MetadataItemInfo.syntax, ItemInfo.syntax)

  def joinAllByPaging(page: Int = 1, searchWord: Option[String] = None): List[Fileinfo] = {
    val (f, fm, mii, ii) = joinTableSyntaxes
    DB.readOnly {implicit session =>
      select.from(Fileinfo as f)
        .leftJoin(FileMetadata as fm).on(f.md5, fm.md5)
        .leftJoin(MetadataItemInfo as mii).on(fm.id, mii.fileMetadataId)
        .leftJoin(ItemInfo as ii).on(mii.itemInfoId, ii.id)
        .where(sqls.toAndConditionOpt(searchWord.map(sqls.like(ii.name, _))))
        .limit(PER_PAGE).offset((page - 1) * PER_PAGE)
        .toSQL
        .one(Fileinfo(f))
        .toManies(
          rs => rs.longOpt(fm.id).map(_ => FileMetadata(fm)(rs)),
          rs => None: Option[MetadataItemInfo],
          rs => rs.longOpt(ii.id).map(_ => ItemInfo(ii)(rs))
        ).map((f, fm, n, ii) => f.copy(fileMetadata = fm.headOption.map(_.copy(itemInfos = ii)))).list().apply()
    }
  }

  def joinAllByPagingWithTotalCount(page: Int = 1, searchWord: Option[String] = None): (List[Fileinfo], Long) = {
    val (f, fm, mii, ii) = joinTableSyntaxes
    val fileinfoCount = DB.readOnly {implicit session =>
      select(sqls.count(sqls.distinct(f.id))).from(Fileinfo as f)
        .leftJoin(FileMetadata as fm).on(f.md5, fm.md5)
        .leftJoin(MetadataItemInfo as mii).on(fm.id, mii.fileMetadataId)
        .leftJoin(ItemInfo as ii).on(mii.itemInfoId, ii.id)
        .where(sqls.toAndConditionOpt(searchWord.map(sqls.like(ii.name, _))))
        .toSQL
        .map(_.int(1)).single.apply().get
    }
    (joinAllByPaging(page, searchWord), fileinfoCount)
  }
}
