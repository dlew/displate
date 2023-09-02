package net.danlew.displate

import net.danlew.displate.model.DualDisplates
import net.danlew.displate.model.LimitedDisplate
import net.danlew.displate.model.LimitedType
import net.danlew.displate.model.LimitedType.*
import net.danlew.displate.model.NormalDisplate
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.io.path.exists

fun main() {
  val displateData = gatherLimitedEditionData() + Api.queryLuminos()!!

  val sortedDisplateData = displateData.sortedWith(
    compareBy<LimitedDisplate> { it.edition.startDate }.thenBy { it.itemCollectionId }
  )

  val dualDisplateData = gatherNormalEditions(sortedDisplateData)
  val csvData = displatesToCsvData(dualDisplateData)
  outputToCsv(csvData)

  downloadImages(sortedDisplateData)
}

fun gatherLimitedEditionData(): List<LimitedDisplate> {
  val allLimitedDisplates = Api.queryLimitedEditions()!!

  return allLimitedDisplates.map { displate ->
    Thread.sleep(400)
    Api.limitedDetails(displate.itemCollectionId)!!
  }
}

fun gatherNormalEditions(limitedEditions: List<LimitedDisplate>): List<DualDisplates> {
  return limitedEditions.map { limited ->
    var normal: NormalDisplate? = null
    val normalId = Data.limitedToNormal[limited.itemCollectionId]
    if (normalId != null) {
      normal = Api.normalDetails(normalId)
      Thread.sleep(400)
    }

    return@map DualDisplates(
      limited = limited,
      normal = normal
    )
  }

}

fun displatesToCsvData(dualDisplates: List<DualDisplates>): List<List<String?>> {
  val headers = listOf(
    "ID",
    "Release Date",
    "Image",
    "Name",
    "Link",
    "Quantity",
    "Artist",
    "Artist Link",
    "Normal Image",
    "Normal Name",
    "Normal Link",
    "Type",
    "Cost",
  )

  val rows = dualDisplates.map { (limited, normal) ->
    listOf(
      limited.itemCollectionId.toString() ?: "Unknown",
      limited.edition.startDate.toLocalDate().toString(),
      limited.images.main.url,
      limited.title,
      limited.url.let { "https://displate.com$it" },
      limited.edition.size.toString(),
      limited.author?.fullName?.trim() ?: "Unknown",
      limited.author?.url,
      normal?.imageUrl,
      normal?.title,
      normal?.itemCollectionId?.let { "https://displate.com/displate/$it" },
      limited.edition.type.type,
      getCost(limited.edition.type, limited.edition.startDate).toString()
    )
  }

  return listOf(headers) + rows
}

private val PRICE_CUTOFF = LocalDate.of(2023, 7, 1)

fun getCost(type: LimitedType, startDate: LocalDateTime): Int {
  val lowerPrice = startDate.toLocalDate().isBefore(PRICE_CUTOFF)
  return when (type) {
    standard -> if (lowerPrice) 139 else 149
    ultra -> if (lowerPrice) 289 else 299
    lumino -> 299
  }
}

fun outputToCsv(data: List<List<String?>>) {
  CSVPrinter(FileWriter("output.csv"), CSVFormat.DEFAULT).use { printer ->
    data.forEach { row ->
      printer.printRecord(row)
    }
  }
}

fun downloadImages(displate: List<LimitedDisplate>) {
  val outputPath = Files.createDirectories(Paths.get("images/"))

  displate.forEach { displate ->
    val imageUrl = displate.images.main.url.toHttpUrl()

    val fileName = imageUrl.pathSegments.last()
    val destination = outputPath.resolve(fileName)

    if (!destination.exists()) {
      Api.image(imageUrl, destination)
    }
  }
}