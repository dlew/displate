package net.danlew.displate

import net.danlew.displate.model.LimitedType
import net.danlew.displate.model.LimitedType.lumino
import net.danlew.displate.model.LimitedType.standard
import net.danlew.displate.model.LimitedType.ultra
import net.danlew.displate.model.OrderedDualDisplates
import okhttp3.HttpUrl.Companion.toHttpUrl
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime
import kotlin.io.path.exists

object Output {
  fun outputToCsv(filename: String, orderedDualDisplates: List<OrderedDualDisplates>) {
    val data = displatesToCsvData(orderedDualDisplates)
    CSVPrinter(FileWriter(filename), CSVFormat.DEFAULT).use { printer ->
      data.forEach { row ->
        printer.printRecord(row)
      }
    }
  }

  fun downloadImages(displates: List<OrderedDualDisplates>) {
    val outputPath = Files.createDirectories(Paths.get("images/"))

    displates.forEach { displate ->
      val imageUrl = displate.dualDisplates.limited.images.main.url.toHttpUrl()

      val fileName = imageUrl.pathSegments.last()
      val destination = outputPath.resolve(fileName)

      if (!destination.exists()) {
        Api.image(imageUrl, destination)
      }
    }
  }

  private fun displatesToCsvData(orderedDualDisplates: List<OrderedDualDisplates>): List<List<String?>> {
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
      "Position",
    )

    val rows = orderedDualDisplates.map { (dualDisplates, position) ->
      val limited = dualDisplates.limited
      val normal = dualDisplates.normal

      return@map listOf(
        limited.itemCollectionId.toString() ?: "Unknown",
        limited.edition.startDate.toLocalDate().toString(),
        limited.images.main.url,
        limited.title,
        limited.url.let { "https://displate.com$it" },
        if (limited.edition.type == lumino) "N/A" else limited.edition.size.toString(),
        limited.author?.fullName?.trim() ?: "Unknown",
        limited.author?.url,
        normal?.imageUrl,
        normal?.title,
        normal?.itemCollectionId?.let { "https://displate.com/displate/$it" },
        limited.edition.type.type,
        getCost(limited.edition.type, limited.edition.startDate).toString(),
        position.toString(),
      )
    }

    return listOf(headers) + rows
  }

  private val PRICE_CUTOFF = LocalDate.of(2023, 7, 1)

  private fun getCost(type: LimitedType, startDate: LocalDateTime): Int {
    val lowerPrice = startDate.toLocalDate().isBefore(PRICE_CUTOFF)
    return when (type) {
      standard -> if (lowerPrice) 139 else 149
      ultra -> if (lowerPrice) 289 else 299
      lumino -> 299
    }
  }
}