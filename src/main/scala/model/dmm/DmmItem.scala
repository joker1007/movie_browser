package model.dmm


case class DmmItem(
  contentId: String,
  title: String,
  url: String,
  imageUrl: Option[String],
  largeImageUrl: Option[String],
  itemInfo: List[DmmItemInfo],
  sampleImageUrls: List[String])
