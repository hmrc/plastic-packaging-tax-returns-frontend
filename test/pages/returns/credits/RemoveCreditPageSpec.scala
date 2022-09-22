package pages.returns.credits

import pages.behaviours.PageBehaviours

class RemoveCreditPageSpec extends PageBehaviours {

  "RemoveCreditPage" - {

    beRetrievable[Boolean](RemoveCreditPage)

    beSettable[Boolean](RemoveCreditPage)

    beRemovable[Boolean](RemoveCreditPage)
  }
}
