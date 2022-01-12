package net.danlew.displate

import net.danlew.displate.model.Displate

fun main() {
  println(gatherLimitedEditionData())
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
