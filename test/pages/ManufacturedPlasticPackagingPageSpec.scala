package pages

import pages.behaviours.PageBehaviours

class ManufacturedPlasticPackagingPageSpec extends PageBehaviours {

  "ManufacturedPlasticPackagingPage" - {

    beRetrievable[Boolean](ManufacturedPlasticPackagingPage)

    beSettable[Boolean](ManufacturedPlasticPackagingPage)

    beRemovable[Boolean](ManufacturedPlasticPackagingPage)
  }
}
