package pages

import pages.behaviours.PageBehaviours

class $className$PageSpec extends PageBehaviours {

  "$className$Page" - {

    beRetrievable[Long]($className$Page)

    beSettable[Long]($className$Page)

    beRemovable[Long]($className$Page)
  }
}
