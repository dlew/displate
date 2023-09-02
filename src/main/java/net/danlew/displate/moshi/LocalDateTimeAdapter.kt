package net.danlew.displate.moshi

import com.squareup.moshi.FromJson
import com.squareup.moshi.ToJson
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object LocalDateTimeAdapter {

  private val FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

  @ToJson
  fun toJson(value: LocalDateTime): String = throw UnsupportedOperationException()

  @FromJson
  fun fromJson(value: String): LocalDateTime = LocalDateTime.parse(value, FORMATTER)

}