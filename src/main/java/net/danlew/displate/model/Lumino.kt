package net.danlew.displate.model

import com.squareup.moshi.JsonClass
import net.danlew.displate.moshi.LuminoDateTime
import java.time.LocalDateTime

@JsonClass(generateAdapter = true)
data class Lumino(
  val externalId: Int,
  val title: String,
  @param:LuminoDateTime val startDate: LocalDateTime,
  val author: Author,
  val image: ScaledImages,
) {

  fun toLimitedDisplate() = LimitedDisplate(
    title = this.title,
    edition = Edition(
      startDate = this.startDate,
      size = 0,
      type = LimitedType.lumino
    ),
    images = Images(
      main = Image(this.image.x2)
    ),
    itemCollectionId = this.externalId,
    url = "/lumino/${this.externalId}",
    author = this.author
  )
}

