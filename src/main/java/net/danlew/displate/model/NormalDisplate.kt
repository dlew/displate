package net.danlew.displate.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NormalDisplate(
  val itemCollectionId: Int,
  val title: String,
  val imageUrl: String,
  val author: Author
)
