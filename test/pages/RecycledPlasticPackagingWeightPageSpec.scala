package pages

import pages.behaviours.PageBehaviours

class RecycledPlasticPackagingWeightPageSpec extends PageBehaviours {

  "RecycledPlasticPackagingWeightPage" - {

    beRetrievable[Int](RecycledPlasticPackagingWeightPage)

    beSettable[Int](RecycledPlasticPackagingWeightPage)

    beRemovable[Int](RecycledPlasticPackagingWeightPage)
  }
}
