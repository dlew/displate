package net.danlew.displate.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Author(
  val fullName: String,
  val url: String
)
