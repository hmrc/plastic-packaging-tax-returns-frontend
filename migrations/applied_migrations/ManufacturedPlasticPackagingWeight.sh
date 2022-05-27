#!/bin/bash

echo ""
echo "Applying migration ManufacturedPlasticPackagingWeight"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /manufacturedPlasticPackagingWeight                  controllers.ManufacturedPlasticPackagingWeightController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /manufacturedPlasticPackagingWeight                  controllers.ManufacturedPlasticPackagingWeightController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeManufacturedPlasticPackagingWeight                        controllers.ManufacturedPlasticPackagingWeightController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeManufacturedPlasticPackagingWeight                        controllers.ManufacturedPlasticPackagingWeightController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "manufacturedPlasticPackagingWeight.title = ManufacturedPlasticPackagingWeight" >> ../conf/messages.en
echo "manufacturedPlasticPackagingWeight.heading = ManufacturedPlasticPackagingWeight" >> ../conf/messages.en
echo "manufacturedPlasticPackagingWeight.checkYourAnswersLabel = ManufacturedPlasticPackagingWeight" >> ../conf/messages.en
echo "manufacturedPlasticPackagingWeight.error.nonNumeric = Enter your manufacturedPlasticPackagingWeight using numbers" >> ../conf/messages.en
echo "manufacturedPlasticPackagingWeight.error.required = Enter your manufacturedPlasticPackagingWeight" >> ../conf/messages.en
echo "manufacturedPlasticPackagingWeight.error.wholeNumber = Enter your manufacturedPlasticPackagingWeight using whole numbers" >> ../conf/messages.en
echo "manufacturedPlasticPackagingWeight.error.outOfRange = ManufacturedPlasticPackagingWeight must be between {0} and {1}" >> ../conf/messages.en
echo "manufacturedPlasticPackagingWeight.change.hidden = ManufacturedPlasticPackagingWeight" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryManufacturedPlasticPackagingWeightUserAnswersEntry: Arbitrary[(ManufacturedPlasticPackagingWeightPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ManufacturedPlasticPackagingWeightPage.type]";\
    print "        value <- arbitrary[Long].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryManufacturedPlasticPackagingWeightPage: Arbitrary[ManufacturedPlasticPackagingWeightPage.type] =";\
    print "    Arbitrary(ManufacturedPlasticPackagingWeightPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ManufacturedPlasticPackagingWeightPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration ManufacturedPlasticPackagingWeight completed"
