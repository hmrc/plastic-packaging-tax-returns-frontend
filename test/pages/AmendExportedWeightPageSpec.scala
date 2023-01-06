package pages

import pages.behaviours.PageBehaviours

class AmendExportedWeightPageSpec extends PageBehaviours {

  "AmendExportedWeightPage" - {

    beRetrievable[Long](AmendExportedWeightPage)

    beSettable[Long](AmendExportedWeightPage)

    beRemovable[Long](AmendExportedWeightPage)
  }
}
