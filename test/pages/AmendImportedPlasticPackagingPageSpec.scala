package pages

import pages.behaviours.PageBehaviours

class AmendImportedPlasticPackagingPageSpec extends PageBehaviours {

  "AmendImportedPlasticPackagingPage" - {

    beRetrievable[Int](AmendImportedPlasticPackagingPage)

    beSettable[Int](AmendImportedPlasticPackagingPage)

    beRemovable[Int](AmendImportedPlasticPackagingPage)
  }
}
