package net.danlew.displate.model

import com.squareup.moshi.JsonClass
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class Edition(
  val startDate: LocalDateTime,
  val size: Int,
  val type: LimitedType
)
