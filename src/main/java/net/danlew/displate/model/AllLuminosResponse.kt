package net.danlew.displate.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AllLuminosResponse(
  val active: List<Lumino>,
  val upcoming: List<Lumino>,
  val soldOut: List<Lumino>,
)
