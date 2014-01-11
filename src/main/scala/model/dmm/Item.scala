package model.dmm

import org.joda.time.DateTime

case class Item(
  contentId: String,
  title: String,
  url: String,
  imageUrl: Option[String],
  largeImageUrl: Option[String],
  itemInfo: List[ItemInfo],
  sampleImageUrls: List[String])
