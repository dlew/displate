package net.danlew.displate.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object LuminoLocalDateTimeAdapter {

  @ToJson
  fun toJson(@LuminoDateTime value: LocalDateTime): String = throw UnsupportedOperationException()

  @FromJson
  @LuminoDateTime
  fun fromJson(value: String): LocalDateTime = LocalDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME)

}