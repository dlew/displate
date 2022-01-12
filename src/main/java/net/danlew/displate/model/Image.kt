package net.danlew.displate.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Image(
  val url: String
)
