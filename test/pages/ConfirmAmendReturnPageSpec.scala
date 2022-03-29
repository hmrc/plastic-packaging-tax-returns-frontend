package pages

import pages.behaviours.PageBehaviours

class ConfirmAmendReturnPageSpec extends PageBehaviours {

  "ConfirmAmendReturnPage" - {

    beRetrievable[Boolean](ConfirmAmendReturnPage)

    beSettable[Boolean](ConfirmAmendReturnPage)

    beRemovable[Boolean](ConfirmAmendReturnPage)
  }
}
