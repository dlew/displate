package net.danlew.displate.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Edition(
  val startDate: String,
  val size: Int
)
