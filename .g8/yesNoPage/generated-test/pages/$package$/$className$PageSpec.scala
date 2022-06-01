package pages.$package$

import pages.behaviours.PageBehaviours
import pages.$package$.$className$Page

class $className$PageSpec extends PageBehaviours {

  "$className$Page" - {

    beRetrievable[Boolean]($className$Page)

    beSettable[Boolean]($className$Page)

    beRemovable[Boolean]($className$Page)
  }
}
