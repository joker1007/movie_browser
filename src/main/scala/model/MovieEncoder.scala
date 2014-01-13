package model

import scala.sys.process._
import scalax.file.Path
import scala.collection.mutable

class MovieEncoder(fileinfo: Fileinfo, options: Map[String, String] = Map()) {
  private[this] val defaultOptions = Map(
    "format" -> "mp4",
    "vcodec" -> "libx264",
    "videoBitrate" -> "600k",
    "acodec" -> "libfaac",
    "audioBitrate" -> "128k",
    "audioSampleRate" -> "44100"
  )

  lazy val encodeOptions = defaultOptions ++ options


  def encode(): Path = {
    MovieEncoder.cache.get(fileinfo.id).getOrElse {
      val o = encodeOptions
      val tmpFile = MovieEncoder.workDir / s"${fileinfo.md5}.${o("format")}"
      if (tmpFile.exists)
        return tmpFile

      val encodeCmd = Seq(
        "ffmpeg", "-y", "-i", fileinfo.fullpath, "-f", o("format"), "-vf", "scale=640:-1",
        "-vcodec", o("vcodec"), "-b:v", o("videoBitrate"),
        "-acodec", o("acodec"), "-b:a", o("audioBitrate"), "-ar", o("audioSampleRate"),
        "-partitions", "all", "-me_method", "hex", "-subq", "6", "-me_range", "16",
        "-g", "250", "-keyint_min", "25", "-sc_threshold", "40", "-b_strategy", "1", "-movflags", "frag_keyframe",
        "-coder", "1", "-level", "30", "-async", "2", tmpFile.path
      )
      encodeCmd.run
      MovieEncoder.cache.put(fileinfo.id, tmpFile)
      Thread.sleep(5000)
      tmpFile
    }
  }
}

object MovieEncoder {
  lazy val workDir = Path("/Users/joker/srcs/movie_browser/src/main/webapp/videos", '/')
  lazy val cache = mutable.Map[Long, Path]()
}
