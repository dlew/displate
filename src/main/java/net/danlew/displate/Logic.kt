package net.danlew.displate

import net.danlew.displate.Data.variants
import net.danlew.displate.model.Author
import net.danlew.displate.model.DualDisplates
import net.danlew.displate.model.Edition
import net.danlew.displate.model.Image
import net.danlew.displate.model.Images
import net.danlew.displate.model.LimitedDisplate
import net.danlew.displate.model.LimitedType
import net.danlew.displate.model.LimitedType.event_exclusive
import net.danlew.displate.model.LimitedType.lumino
import net.danlew.displate.model.LimitedType.standard
import net.danlew.displate.model.LimitedType.ultra
import net.danlew.displate.model.NormalDisplate
import net.danlew.displate.model.OrderedDualDisplates
import org.apache.commons.csv.CSVFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

object Logic {
  fun fetchDisplateData(): List<OrderedDualDisplates> {
    val displateData = gatherLimitedEditionData() + Api.queryLuminos()!!
    val displateIds = displateData.map { it.itemCollectionId }.toSet()
    val archivalDataToUse = loadArchivalDisplateData().filter { it.limited.itemCollectionId !in displateIds }
    val dualDisplateData = gatherNormalEditions(displateData) + archivalDataToUse
    return orderDisplates(dualDisplateData)
  }

  fun getCost(type: LimitedType, startDate: LocalDateTime): Int {
    val lowerPrice = startDate.toLocalDate().isBefore(PRICE_CUTOFF)
    return when (type) {
      event_exclusive -> 99
      standard -> if (lowerPrice) 139 else 149
      ultra -> if (lowerPrice) 289 else 299
      lumino -> 299
    }
  }

  private val PRICE_CUTOFF = LocalDate.of(2023, 7, 1)

  private fun gatherLimitedEditionData(): List<LimitedDisplate> {
    val allLimitedDisplates = Api.queryLimitedEditions()!!

    return allLimitedDisplates.map { displate ->
      Api.limitedDetails(displate.itemCollectionId)!!
    }
  }

  // Displate removed a bunch of LEs from their database; we use archival data to restore them
  private fun loadArchivalDisplateData(): List<DualDisplates> {
    val reader = Thread.currentThread()
      .contextClassLoader
      .getResourceAsStream("old-displates.csv")
      .bufferedReader()

    val records = CSVFormat.Builder
      .create(CSVFormat.DEFAULT)
      .setHeader()
      .setSkipHeaderRecord(true)
      .build()
      .parse(reader)

    return records.map { record ->
      val quantity = record["Quantity"]
      val author = Author(
        fullName = record["Artist"],
        url = record["Artist Link"]
      )

      var normal: NormalDisplate? = null
      if (!record["Normal Name"].isNullOrEmpty()) {
        normal = NormalDisplate(
          itemCollectionId = record["Normal Link"].substring(30).toInt(),
          title = record["Normal Name"],
          imageUrl = record["Normal Image"],
          author = author
        )
      }

      DualDisplates(
        limited = LimitedDisplate(
          title = record["Name"],
          edition = Edition(
            startDate = LocalDateTime.of(LocalDate.parse(record["Release Date"]), LocalTime.MIDNIGHT),
            size = if (quantity == "N/A") 0 else quantity.toInt(),
            type = LimitedType.parse(record["Type"])
          ),
          images = Images(
            main = Image(
              url = record["Image"]
            )
          ),
          itemCollectionId = record["ID"].toInt(),
          url = record["Link"].substring(20),
          author = author
        ),
        normal = normal
      )
    }
  }

  private fun gatherNormalEditions(limitedEditions: List<LimitedDisplate>): List<DualDisplates> {
    return limitedEditions.map { limited ->
      var normal: NormalDisplate? = null
      val normalId = Data.limitedToNormal[limited.itemCollectionId]
      if (normalId != null) {
        normal = Api.normalDetails(normalId)
      }

      return@map DualDisplates(
        limited = limited,
        normal = normal
      )
    }
  }

  private fun orderDisplates(dualDisplates: List<DualDisplates>): List<OrderedDualDisplates> {
    val sortedDisplateData = dualDisplates.sortedWith(
      compareBy<DualDisplates> { it.limited.edition.startDate }.thenBy { it.limited.itemCollectionId }
    )

    val currentPositions = mutableMapOf<LimitedType, Int>()
    val limitedPositions = mutableMapOf<Int, Int>()

    return sortedDisplateData.map { dualDisplates ->
      val itemCollectionId = dualDisplates.limited.itemCollectionId
      var position = limitedPositions[itemCollectionId]
      if (position == null) {
        val type = dualDisplates.limited.edition.type
        position = currentPositions.getOrDefault(type, 0) + 1
        currentPositions[type] = position

        // Find if this has any variants; if it does, pre-set the position of other variants
        variants
          .find { it.contains(itemCollectionId) }
          ?.forEach { variantId ->
            limitedPositions[variantId] = position
          }
      }

      return@map OrderedDualDisplates(dualDisplates, position)
    }
  }
}