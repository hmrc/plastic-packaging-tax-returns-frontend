package pages

import pages.behaviours.PageBehaviours

class HumanMedicinesPlasticPackagingWeightPageSpec extends PageBehaviours {

  "HumanMedicinesPlasticPackagingWeightPage" - {

    beRetrievable[Int](HumanMedicinesPlasticPackagingWeightPage)

    beSettable[Int](HumanMedicinesPlasticPackagingWeightPage)

    beRemovable[Int](HumanMedicinesPlasticPackagingWeightPage)
  }
}
