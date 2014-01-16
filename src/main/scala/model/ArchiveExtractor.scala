package model

import scalax.file.Path
import java.io.{FileOutputStream, BufferedOutputStream, FileInputStream, File => JFile}
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import scala.util.control.Exception._
import scalikejdbc._
import org.slf4j.LoggerFactory
import scalax.file.PathMatcher.IsFile


class ArchiveExtractor(fileinfo: Fileinfo, force: Boolean = false) {
  def extract(): Either[Throwable, Path] = {
    val outputDir = ArchiveExtractor.workDir / fileinfo.md5
    if (!outputDir.isDirectory)
      outputDir.createDirectory()

    if (outputDir.children(IsFile).size > 0 && !force)
      return Right(outputDir)

    val file = new JFile(fileinfo.fullpath)
    val zis = new ZipArchiveInputStream(new FileInputStream(file), "Windows-31J")
    allCatch either {
      using(zis) {zs =>
        val iter = Iterator.continually(zs.getNextZipEntry()).takeWhile(_ != null)
          .filterNot(_.isDirectory())
          .filter {e =>
            Fileinfo.IMAGE_EXTENSIONS.contains(Path(e.getName(), '/').extension.getOrElse(""))
          }.filterNot {e =>
            val invalidPattern = """((^\.)|(__MACOSX)|(DS_Store))""".r
            invalidPattern.findFirstIn(e.getName()).isDefined
          }.zipWithIndex

        for ((e, idx) <- iter) {
          val ext = Path(e.getName, '/').extension.get
          val fileName = "%03d".format(idx) + s".$ext"
          val output = outputDir / fileName
          val fos = new BufferedOutputStream(new FileOutputStream(new JFile(output.path)))
          using(fos) {os =>
            Iterator.continually(zs.read()).takeWhile(_ != -1).foreach(os write _)
          }
          val logger = LoggerFactory.getLogger(getClass)
          logger.info(s"Extract: ${output.path}")
        }
      }
      outputDir
    }
  }
}

object ArchiveExtractor {
  lazy val workDir = Path(Option(System.getProperty("archive.output")).getOrElse("src/main/webapp/extracted"), '/')
}
