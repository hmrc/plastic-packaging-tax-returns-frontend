package pages

import pages.behaviours.PageBehaviours

class ManufacturedPlasticPackagingWeightPageSpec extends PageBehaviours {

  "ManufacturedPlasticPackagingWeightPage" - {

    beRetrievable[Int](ManufacturedPlasticPackagingWeightPage)

    beSettable[Int](ManufacturedPlasticPackagingWeightPage)

    beRemovable[Int](ManufacturedPlasticPackagingWeightPage)
  }
}
