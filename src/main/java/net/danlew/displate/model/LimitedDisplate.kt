package net.danlew.displate.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LimitedDisplate(
  val title: String,
  val edition: Edition,
  val images: Images,

  val itemCollectionId: Int,
  val url: String,

  // Only present on full detail calls
  val author: Author?
)
