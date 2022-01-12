package net.danlew.displate

import com.squareup.moshi.Moshi
import net.danlew.displate.model.Displate
import net.danlew.displate.model.DisplateListResponse
import net.danlew.displate.model.DisplateSingleResponse
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import java.io.File

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
  val moshi = Moshi.Builder().build()

  fun queryLimitedEditions(): List<Displate>? {
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
        .adapter(DisplateListResponse::class.java)
        .fromJson(response.body!!.source())!!
        .data
    }
  }

  fun limitedEditionDetails(itemCollectionId: Int): Displate? {
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
        .adapter(DisplateSingleResponse::class.java)
        .fromJson(response.body!!.source())!!
        .data
    }
  }

}