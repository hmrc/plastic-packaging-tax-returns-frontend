package pages

import pages.behaviours.PageBehaviours

class DirectlyExportedComponentsPageSpec extends PageBehaviours {

  "DirectlyExportedComponentsPage" - {

    beRetrievable[Boolean](DirectlyExportedComponentsPage)

    beSettable[Boolean](DirectlyExportedComponentsPage)

    beRemovable[Boolean](DirectlyExportedComponentsPage)
  }
}
