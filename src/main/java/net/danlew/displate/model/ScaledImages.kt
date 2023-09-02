package net.danlew.displate.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ScaledImages(
  val x2: String
)
