package pages

import pages.behaviours.PageBehaviours

class ImportedPlasticPackagingWeightPageSpec extends PageBehaviours {

  "ImportedPlasticPackagingWeightPage" - {

    beRetrievable[Int](ImportedPlasticPackagingWeightPage)

    beSettable[Int](ImportedPlasticPackagingWeightPage)

    beRemovable[Int](ImportedPlasticPackagingWeightPage)
  }
}
