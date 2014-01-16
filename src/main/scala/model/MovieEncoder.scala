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
    val o = encodeOptions
    val segmentListName = s"${fileinfo.md5}.m3u8"
    val segmentListPath = MovieEncoder.workDir / segmentListName
    if (segmentListPath.exists)
      return segmentListPath

    val streamFileDir = MovieEncoder.workDir / fileinfo.md5
    if (!streamFileDir.isDirectory)
      streamFileDir.createDirectory()
    val streamFileFormat = s"${fileinfo.md5}/stream%04d.ts"


    val encodeCmd = Seq(
      "ffmpeg", "-y", "-i", fileinfo.fullpath, "-vprofile", "main",
      "-vf", "scale=640:-1", "-vcodec", o("vcodec"), "-b:v", o("videoBitrate"),
      "-qcomp", "0.6", "-qmin", "10", "-qmax", "51", "-qdiff", "4", "-i_qfactor", "0.71",
      "-acodec", o("acodec"), "-b:a", o("audioBitrate"), "-ar", o("audioSampleRate"),
      "-partitions", "+pi8x8+pi4x4+pp8x8+pb8x8", "-me_method", "hex", "-subq", "6", "-me_range", "16",
      "-g", "250", "-keyint_min", "25", "-sc_threshold", "40", "-b_strategy", "1",
      "-cmp", "chroma", "-flags", "+loop-global_header", "-movflags", "faststart",
      "-coder", "1", "-level", "31", "-async", "2", "-map", "0", "-bsf", "h264_mp4toannexb",
      "-f", "segment", "-segment_format", "mpegts", "-segment_time", "10", "-segment_list", segmentListName, streamFileFormat
    )
    Process(encodeCmd, MovieEncoder.workDir.jfile).run
    Thread.sleep(5000)
    segmentListPath
  }
}

object MovieEncoder {
  lazy val workDir = Path(Option(System.getProperty("movie.output")).getOrElse("src/main/webapp/videos"), '/')
}
