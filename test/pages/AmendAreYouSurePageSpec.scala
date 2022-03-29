package pages

import pages.behaviours.PageBehaviours

class AmendAreYouSurePageSpec extends PageBehaviours {

  "AmendAreYouSurePage" - {

    beRetrievable[Boolean](AmendAreYouSurePage)

    beSettable[Boolean](AmendAreYouSurePage)

    beRemovable[Boolean](AmendAreYouSurePage)
  }
}
