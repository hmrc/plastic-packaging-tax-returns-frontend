package pages

import pages.behaviours.PageBehaviours

class AmendDirectExportPlasticPackagingPageSpec extends PageBehaviours {

  "AmendDirectExportPlasticPackagingPage" - {

    beRetrievable[Int](AmendDirectExportPlasticPackagingPage)

    beSettable[Int](AmendDirectExportPlasticPackagingPage)

    beRemovable[Int](AmendDirectExportPlasticPackagingPage)
  }
}
