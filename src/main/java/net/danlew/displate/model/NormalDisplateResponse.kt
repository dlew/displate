package net.danlew.displate.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class NormalDisplateResponse(
  val data: NormalDisplate
)
