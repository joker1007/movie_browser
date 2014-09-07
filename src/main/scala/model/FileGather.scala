package model

import java.io.File
import java.nio.file.Paths

import org.apache.commons.io.FileUtils

object FileGather {
  def gather(target: Target): Array[(Target, File)] = {
    val files =
      FileUtils.listFiles(Paths.get(target.fullpath).toFile, Media.EXTENSIONS, true).toArray(Array[File]())

    files.map { f =>
      (target, f)
    }
  }
}
