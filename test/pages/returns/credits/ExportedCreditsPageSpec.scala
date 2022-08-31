package pages.returns.credits

import pages.behaviours.PageBehaviours

class ExportedCreditsPageSpec extends PageBehaviours {

  "ExportedCreditsPage" - {

    beRetrievable[Boolean](ExportedCreditsPage)

    beSettable[Boolean](ExportedCreditsPage)

    beRemovable[Boolean](ExportedCreditsPage)
  }
}
