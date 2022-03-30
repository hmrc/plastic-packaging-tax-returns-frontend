package pages

import pages.behaviours.PageBehaviours

class ExportedPlasticPackagingWeightPageSpec extends PageBehaviours {

  "ExportedPlasticPackagingWeightPage" - {

    beRetrievable[Int](ExportedPlasticPackagingWeightPage)

    beSettable[Int](ExportedPlasticPackagingWeightPage)

    beRemovable[Int](ExportedPlasticPackagingWeightPage)
  }
}
