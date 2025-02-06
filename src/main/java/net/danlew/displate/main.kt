package net.danlew.displate

import net.danlew.displate.Logic.fetchDisplateData
import net.danlew.displate.Output.downloadImages
import net.danlew.displate.Output.outputToCsv

fun main() {
  val displateData = fetchDisplateData()
  outputToCsv("output.csv", displateData)
  downloadImages(displateData)
}
