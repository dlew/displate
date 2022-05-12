package net.danlew.displate

import net.danlew.displate.model.DualDisplates
import net.danlew.displate.model.LimitedDisplate
import net.danlew.displate.model.NormalDisplate
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.exists

fun main() {
  val displateData = gatherLimitedEditionData().sortedBy { it.edition.startDate }
  val dualDisplateData = gatherNormalEditions(displateData)
  val csvData = displatesToCsvData(dualDisplateData)
  outputToCsv(csvData)

  downloadImages(displateData)
}

fun gatherLimitedEditionData(): List<LimitedDisplate> {
  val allLimitedDisplates = Api.queryLimitedEditions()!!

  return allLimitedDisplates.map { displate ->
    if (displate.itemCollectionId != null) {
      val limitedDisplate = Api.limitedDetails(displate.itemCollectionId)!!
      Thread.sleep(400)
      val isSoldOut = Api.isSoldOut(displate.itemCollectionId)
      Thread.sleep(400)
      limitedDisplate.copy(isSoldOut = isSoldOut)
    } else {
      displate.copy(isSoldOut = false) // Assume that pre-release LEs are not sold out :P
    }
  }
}

fun gatherNormalEditions(limitedEditions: List<LimitedDisplate>): List<DualDisplates> {
  return limitedEditions.map { limited ->
    var normal: NormalDisplate? = null
    if (limited.itemCollectionId != null) {
      val normalId = Data.limitedToNormal[limited.itemCollectionId]
      if (normalId != null) {
        normal = Api.normalDetails(normalId)
        Thread.sleep(400)
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
    "Normal Link",
    "Sold Out"
  )

  val rows = dualDisplates.map { (limited, normal) ->
    listOf(
      limited.itemCollectionId?.toString() ?: "Unknown",
      limited.edition.startDate.toLocalDate().toString(),
      limited.images.main.url,
      limited.title,
      limited.url?.let { "https://displate.com$it" },
      limited.edition.size.toString(),
      limited.author?.fullName?.trim() ?: "Unknown",
      limited.author?.url,
      normal?.imageUrl,
      normal?.title,
      normal?.itemCollectionId?.let { "https://displate.com/displate/$it" },
      limited.isSoldOut?.toString() ?: "unknown",
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