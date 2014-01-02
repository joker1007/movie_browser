package lib

import scalax.file.Path
import scalax.file.PathMatcher.{IsFile, IsDirectory}
import model.Fileinfo

object FileGather {
  def gather(pathName: String) {
    val path = Path(pathName, '/')
    gather(path)
  }

  def gather(path: Path) {
    val pathSet = path.children()
    pathSet.foreach {
      case IsDirectory(f) =>
        gather(f)
      case IsFile(f) =>
        process(f)
    }
  }

  def process(file: Path) {
    val oid = Fileinfo.createFromFile(file)
    oid match {
      case Some(id) => println(s"Create: ${file.toString()}")
      case None =>
    }
  }
}
