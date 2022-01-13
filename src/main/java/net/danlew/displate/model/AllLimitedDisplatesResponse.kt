package net.danlew.displate.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AllLimitedDisplatesResponse(
  val data: List<LimitedDisplate>
)
