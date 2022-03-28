package pages

import pages.behaviours.PageBehaviours

class AmendRecycledPlasticPackagingPageSpec extends PageBehaviours {

  "AmendRecycledPlasticPackagingPage" - {

    beRetrievable[Int](AmendRecycledPlasticPackagingPage)

    beSettable[Int](AmendRecycledPlasticPackagingPage)

    beRemovable[Int](AmendRecycledPlasticPackagingPage)
  }
}
