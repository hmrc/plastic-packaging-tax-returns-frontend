package pages.returns.credits

import pages.behaviours.PageBehaviours

class ConvertedCreditsPageSpec extends PageBehaviours {

  "ConvertedCreditsPage" - {

    beRetrievable[Boolean](ConvertedCreditsPage)

    beSettable[Boolean](ConvertedCreditsPage)

    beRemovable[Boolean](ConvertedCreditsPage)
  }
}
