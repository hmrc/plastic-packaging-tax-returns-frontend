#!/bin/bash

echo ""
echo "Applying migration ExportedPlasticPackagingWeight"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /exportedPlasticPackagingWeight                  controllers.ExportedPlasticPackagingWeightController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /exportedPlasticPackagingWeight                  controllers.ExportedPlasticPackagingWeightController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeExportedPlasticPackagingWeight                        controllers.ExportedPlasticPackagingWeightController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeExportedPlasticPackagingWeight                        controllers.ExportedPlasticPackagingWeightController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "exportedPlasticPackagingWeight.title = ExportedPlasticPackagingWeight" >> ../conf/messages.en
echo "exportedPlasticPackagingWeight.heading = ExportedPlasticPackagingWeight" >> ../conf/messages.en
echo "exportedPlasticPackagingWeight.checkYourAnswersLabel = ExportedPlasticPackagingWeight" >> ../conf/messages.en
echo "exportedPlasticPackagingWeight.error.nonNumeric = Enter your exportedPlasticPackagingWeight using numbers" >> ../conf/messages.en
echo "exportedPlasticPackagingWeight.error.required = Enter your exportedPlasticPackagingWeight" >> ../conf/messages.en
echo "exportedPlasticPackagingWeight.error.wholeNumber = Enter your exportedPlasticPackagingWeight using whole numbers" >> ../conf/messages.en
echo "exportedPlasticPackagingWeight.error.outOfRange = ExportedPlasticPackagingWeight must be between {0} and {1}" >> ../conf/messages.en
echo "exportedPlasticPackagingWeight.change.hidden = ExportedPlasticPackagingWeight" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryExportedPlasticPackagingWeightUserAnswersEntry: Arbitrary[(ExportedPlasticPackagingWeightPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ExportedPlasticPackagingWeightPage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryExportedPlasticPackagingWeightPage: Arbitrary[ExportedPlasticPackagingWeightPage.type] =";\
    print "    Arbitrary(ExportedPlasticPackagingWeightPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ExportedPlasticPackagingWeightPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration ExportedPlasticPackagingWeight completed"
