package pages

import pages.behaviours.PageBehaviours

class endAreYouSurePageSpec extends PageBehaviours {

  "endAreYouSurePage" - {

    beRetrievable[Boolean](endAreYouSurePage)

    beSettable[Boolean](endAreYouSurePage)

    beRemovable[Boolean](endAreYouSurePage)
  }
}
