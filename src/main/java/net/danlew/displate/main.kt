package net.danlew.displate

import net.danlew.displate.model.DualDisplates
import net.danlew.displate.model.LimitedDisplate
import net.danlew.displate.model.NormalDisplate
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter

fun main() {
  val displateData = gatherLimitedEditionData().sortedBy { it.edition.startDate }
  val dualDisplateData = gatherNormalEditions(displateData)
  val csvData = displatesToCsvData(dualDisplateData)
  outputToCsv(csvData)
}

fun gatherLimitedEditionData(): List<LimitedDisplate> {
  val allLimitedDisplates = Api.queryLimitedEditions()!!

  return allLimitedDisplates.map { displate ->
    if (displate.itemCollectionId != null) {
      Api.limitedDetails(displate.itemCollectionId)!!
    } else {
      displate
    }
  }
}

fun gatherNormalEditions(limitedEditions: List<LimitedDisplate>): List<DualDisplates> {
  return limitedEditions.map { limited ->
    var normal: NormalDisplate? = null
    if (limited.itemCollectionId != null) {
      require(limited.itemCollectionId in Data.limitedToNormal) { "No normal info for ${limited.itemCollectionId}" }
      val normalId = Data.limitedToNormal[limited.itemCollectionId]
      if (normalId != null) {
        normal = Api.normalDetails(normalId)
      }
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
    "Normal Link"
  )

  val rows = dualDisplates.map { (limited, normal) ->
    listOf(
      limited.itemCollectionId?.toString() ?: "Unknown",
      limited.edition.startDate.toLocalDate().toString(),
      limited.images.main.url,
      limited.title,
      limited.url?.let { "https://displate.com$it" },
      limited.edition.size.toString(),
      limited.author?.fullName,
      limited.author?.url,
      normal?.imageUrl,
      normal?.title,
      normal?.itemCollectionId?.let { "https://displate.com/displate/$it" }
    )
  }

  return listOf(headers) + rows
}

fun outputToCsv(data: List<List<String?>>) {
  CSVPrinter(FileWriter("output.csv"), CSVFormat.DEFAULT).use { printer ->
    data.forEach { row ->
      printer.printRecord(row)
    }
  }
}