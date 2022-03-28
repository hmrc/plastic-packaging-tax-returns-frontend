package pages

import pages.behaviours.PageBehaviours

class AmendHumanMedicinePlasticPackagingPageSpec extends PageBehaviours {

  "AmendHumanMedicinePlasticPackagingPage" - {

    beRetrievable[Int](AmendHumanMedicinePlasticPackagingPage)

    beSettable[Int](AmendHumanMedicinePlasticPackagingPage)

    beRemovable[Int](AmendHumanMedicinePlasticPackagingPage)
  }
}
