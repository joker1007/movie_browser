package model

import scala.sys.process._
import scalax.file.Path

trait MovieEncoder {
  protected val defaultOptions: Map[String, String]
  protected val overrideOptions: Map[String, String]
  lazy val encodeOptions = defaultOptions ++ overrideOptions

  def encode(): Path
}

class HttpLiveStreamingEncoder(fileinfo: Fileinfo, options: Map[String, String] = Map(), force: Boolean = false) extends MovieEncoder {
  protected val defaultOptions = Map(
    "format" -> "mp4",
    "vcodec" -> "libx264",
    "videoBitrate" -> "800k",
    "acodec" -> "libfaac",
    "audioBitrate" -> "128k",
    "audioSampleRate" -> "44100"
  )

  protected val overrideOptions = options

  def encode(): Path = {
    val o = encodeOptions
    val segmentListName = s"${fileinfo.md5}.m3u8"
    val segmentListPath = MovieEncoder.workDir / segmentListName
    if (segmentListPath.exists && !force)
      return segmentListPath

    val streamFileDir = MovieEncoder.workDir / fileinfo.md5
    if (!streamFileDir.isDirectory)
      streamFileDir.createDirectory()
    val streamFileFormat = s"${fileinfo.md5}/stream%04d.ts"


    val encodeCmd = Seq(
      "ffmpeg", "-y", "-i", fileinfo.fullpath, "-vprofile", "main",
      "-vcodec", o("vcodec"), "-b:v", o("videoBitrate"),
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

class StandardMP4Encoder(fileinfo: Fileinfo, options: Map[String, String] = Map(), force: Boolean = false) extends MovieEncoder {
  protected val defaultOptions = Map(
    "format" -> "mp4",
    "vcodec" -> "libx264",
    "videoBitrate" -> "600k",
    "acodec" -> "libfaac",
    "audioBitrate" -> "128k",
    "audioSampleRate" -> "44100"
  )

  protected val overrideOptions = options

  def encode(): Path = {
    val o = encodeOptions
    val outputFileName = s"${fileinfo.md5}.mp4"
    val outputFilePath = MovieEncoder.workDir / outputFileName
    if (outputFilePath.exists && !force)
      return outputFilePath

    val encodeCmd = Seq(
      "ffmpeg", "-y", "-i", fileinfo.fullpath, "-f", o("format"),
      "-vcodec", o("vcodec"), "-b:v", o("videoBitrate"),
      "-qcomp", "0.6", "-qmin", "10", "-qmax", "51", "-qdiff", "4", "-i_qfactor", "0.71",
      "-acodec", o("acodec"), "-b:a", o("audioBitrate"), "-ar", o("audioSampleRate"),
      "-partitions", "all", "-me_method", "hex", "-subq", "6", "-me_range", "16",
      "-g", "250", "-keyint_min", "25", "-sc_threshold", "40", "-b_strategy", "1", "-movflags", "frag_keyframe",
      "-coder", "1", "-level", "30", "-async", "2", outputFileName
    )
    Process(encodeCmd, MovieEncoder.workDir.jfile).run
    Thread.sleep(5000)
    outputFilePath
  }
}

object MovieEncoder {
  lazy val workDir = Path(Option(System.getProperty("movie.output")).getOrElse("src/main/webapp/videos"), '/')
}
