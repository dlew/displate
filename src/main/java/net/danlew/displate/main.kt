package net.danlew.displate

import net.danlew.displate.model.Displate
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVPrinter
import java.io.FileWriter

fun main() {
  val displateData = gatherLimitedEditionData().sortedBy { it.edition.startDate }
  val csvData = displatesToCsvData(displateData)
  outputToCsv(csvData)
}

fun gatherLimitedEditionData(): List<Displate> {
  val displates = Api.queryLimitedEditions()!!

  return displates.map { displate ->
    if (displate.itemCollectionId != null) {
      Api.limitedEditionDetails(displate.itemCollectionId)!!
    }
    else {
      displate
    }
  }
}

fun displatesToCsvData(displates: List<Displate>): List<List<String?>> {
  val headers = listOf("ID", "Release Date", "Image", "Name", "Link", "Quantity", "Artist", "Artist Link")

  val rows = displates.map { displate ->
    listOf(
      displate.itemCollectionId?.toString() ?: "Unknown",
      displate.edition.startDate.toLocalDate().toString(),
      displate.images.main.url,
      displate.title,
      displate.url,
      displate.edition.size.toString(),
      displate.author?.fullName,
      displate.author?.url
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