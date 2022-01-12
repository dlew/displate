package net.danlew.displate.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Displate(
  val title: String,
  val edition: Edition,
  val images: Images,

  // Null if LE is not yet released
  val itemCollectionId: Int?,
  val url: String?,

  // Only present on full detail calls
  val author: Author?
)
