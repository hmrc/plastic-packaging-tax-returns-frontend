package pages.returns

import pages.behaviours.PageBehaviours

class RecycledPlasticPackagingPageSpec extends PageBehaviours {

  "RecycledPlasticPackagingPage" - {

    beRetrievable[Boolean](RecycledPlasticPackagingPage)

    beSettable[Boolean](RecycledPlasticPackagingPage)

    beRemovable[Boolean](RecycledPlasticPackagingPage)
  }
}
