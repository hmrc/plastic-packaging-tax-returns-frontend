package pages

import pages.behaviours.PageBehaviours

class ImportedPlasticPackagingPageSpec extends PageBehaviours {

  "ImportedPlasticPackagingPage" - {

    beRetrievable[Boolean](ImportedPlasticPackagingPage)

    beSettable[Boolean](ImportedPlasticPackagingPage)

    beRemovable[Boolean](ImportedPlasticPackagingPage)
  }
}
