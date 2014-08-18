package model

import java.io.File
import java.nio.file.Paths

import org.apache.commons.io.FileUtils

object FileGather {
  val extensions = Array("mp4", "mpg", "avi", "mkv", "ogm", "wmv", "mov")
  def gather(target: Target): Unit = {
    val files =
      FileUtils.listFiles(Paths.get(target.fullpath).toFile, extensions, true).toArray(Array[File]())
    files.par.foreach { (f) =>
      println(f)
    }
  }
}
