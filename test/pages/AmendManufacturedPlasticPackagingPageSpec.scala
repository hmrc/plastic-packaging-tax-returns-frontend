package pages

import pages.behaviours.PageBehaviours

class AmendManufacturedPlasticPackagingPageSpec extends PageBehaviours {

  "AmendManufacturedPlasticPackagingPage" - {

    beRetrievable[Int](AmendManufacturedPlasticPackagingPage)

    beSettable[Int](AmendManufacturedPlasticPackagingPage)

    beRemovable[Int](AmendManufacturedPlasticPackagingPage)
  }
}
