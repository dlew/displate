package net.danlew.displate

import com.squareup.moshi.Moshi
import net.danlew.displate.model.*
import net.danlew.displate.moshi.LocalDateTimeAdapter
import net.danlew.displate.moshi.LuminoLocalDateTimeAdapter
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import okio.buffer
import okio.sink
import java.io.File
import java.nio.file.Path

object Api {

  private val client = OkHttpClient.Builder()
    .cache(
      Cache(
        directory = File(".", "http_cache"),
        maxSize = 50L * 1024L * 1024L // 50 MiB
      )
    )
    .addInterceptor(
      HttpLoggingInterceptor().apply { setLevel(Level.BASIC) }
    )
    .build()

  val moshi = Moshi.Builder()
    .add(LocalDateTimeAdapter)
    .add(LuminoLocalDateTimeAdapter)
    .build()

  fun queryLimitedEditions(): List<LimitedDisplate>? {
    client.newCall(
      Request.Builder()
        .url("https://sapi.displate.com/artworks/limited?miso=US")
        .get()
        .build()
    ).execute().use { response ->
      if (!response.isSuccessful) {
        return null
      }

      return moshi
        .adapter(AllLimitedDisplatesResponse::class.java)
        .fromJson(response.body!!.source())!!
        .data
    }
  }

  fun limitedDetails(itemCollectionId: Int): LimitedDisplate? {
    client.newCall(
      Request.Builder()
        .url("https://sapi.displate.com/artworks/limited/$itemCollectionId?miso=US")
        .get()
        .build()
    ).execute().use { response ->
      if (!response.isSuccessful) {
        return null
      }

      return moshi
        .adapter(LimitedDisplateResponse::class.java)
        .fromJson(response.body!!.source())!!
        .data
    }
  }

  fun normalDetails(itemCollectionId: Int): NormalDisplate? {
    client.newCall(
      Request.Builder()
        .url("https://sapi.displate.com/artworks/$itemCollectionId?miso=US")
        .get()
        .build()
    ).execute().use { response ->
      if (!response.isSuccessful) {
        return null
      }

      return moshi
        .adapter(NormalDisplateResponse::class.java)
        .fromJson(response.body!!.source())!!
        .data
    }
  }

  fun queryLuminos(): List<LimitedDisplate>? {
    client.newCall(
      Request.Builder()
        .url("https://displate.com/elysium-api/general/v2/lumino/listing")
        .get()
        .build()
    ).execute().use { response ->
      if (!response.isSuccessful) {
        return null
      }

      val luminos = moshi
        .adapter(AllLuminosResponse::class.java)
        .fromJson(response.body!!.source())!!

      return luminos.active.map(Lumino::toLimitedDisplate) + luminos.soldOut.map(Lumino::toLimitedDisplate) + luminos.upcoming.map(Lumino::toLimitedDisplate)
    }
  }

  fun image(url: HttpUrl, destination: Path) {
    client.newCall(
      Request.Builder()
        .url(url)
        .get()
        .build()
    ).execute().use { response ->
      if (!response.isSuccessful) {
        throw IllegalStateException("Did not successfully download image: $response")
      }

      response.writeToPath(destination)
    }
  }

  private fun Response.writeToPath(path: Path) {
    path.sink().buffer().use { sink ->
      sink.writeAll(body!!.source())
    }
  }
}