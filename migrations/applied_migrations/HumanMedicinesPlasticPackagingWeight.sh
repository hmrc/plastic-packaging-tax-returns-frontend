#!/bin/bash

echo ""
echo "Applying migration HumanMedicinesPlasticPackagingWeight"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /humanMedicinesPlasticPackagingWeight                  controllers.HumanMedicinesPlasticPackagingWeightController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /humanMedicinesPlasticPackagingWeight                  controllers.HumanMedicinesPlasticPackagingWeightController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeHumanMedicinesPlasticPackagingWeight                        controllers.HumanMedicinesPlasticPackagingWeightController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeHumanMedicinesPlasticPackagingWeight                        controllers.HumanMedicinesPlasticPackagingWeightController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "humanMedicinesPlasticPackagingWeight.title = HumanMedicinesPlasticPackagingWeight" >> ../conf/messages.en
echo "humanMedicinesPlasticPackagingWeight.heading = HumanMedicinesPlasticPackagingWeight" >> ../conf/messages.en
echo "humanMedicinesPlasticPackagingWeight.checkYourAnswersLabel = HumanMedicinesPlasticPackagingWeight" >> ../conf/messages.en
echo "humanMedicinesPlasticPackagingWeight.error.nonNumeric = Enter your humanMedicinesPlasticPackagingWeight using numbers" >> ../conf/messages.en
echo "humanMedicinesPlasticPackagingWeight.error.required = Enter your humanMedicinesPlasticPackagingWeight" >> ../conf/messages.en
echo "humanMedicinesPlasticPackagingWeight.error.wholeNumber = Enter your humanMedicinesPlasticPackagingWeight using whole numbers" >> ../conf/messages.en
echo "humanMedicinesPlasticPackagingWeight.error.outOfRange = HumanMedicinesPlasticPackagingWeight must be between {0} and {1}" >> ../conf/messages.en
echo "humanMedicinesPlasticPackagingWeight.change.hidden = HumanMedicinesPlasticPackagingWeight" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryHumanMedicinesPlasticPackagingWeightUserAnswersEntry: Arbitrary[(HumanMedicinesPlasticPackagingWeightPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[HumanMedicinesPlasticPackagingWeightPage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryHumanMedicinesPlasticPackagingWeightPage: Arbitrary[HumanMedicinesPlasticPackagingWeightPage.type] =";\
    print "    Arbitrary(HumanMedicinesPlasticPackagingWeightPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(HumanMedicinesPlasticPackagingWeightPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration HumanMedicinesPlasticPackagingWeight completed"
