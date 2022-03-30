#!/bin/bash

echo ""
echo "Applying migration ImportedPlasticPackagingWeight"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /importedPlasticPackagingWeight                  controllers.ImportedPlasticPackagingWeightController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /importedPlasticPackagingWeight                  controllers.ImportedPlasticPackagingWeightController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeImportedPlasticPackagingWeight                        controllers.ImportedPlasticPackagingWeightController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeImportedPlasticPackagingWeight                        controllers.ImportedPlasticPackagingWeightController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "importedPlasticPackagingWeight.title = ImportedPlasticPackagingWeight" >> ../conf/messages.en
echo "importedPlasticPackagingWeight.heading = ImportedPlasticPackagingWeight" >> ../conf/messages.en
echo "importedPlasticPackagingWeight.checkYourAnswersLabel = ImportedPlasticPackagingWeight" >> ../conf/messages.en
echo "importedPlasticPackagingWeight.error.nonNumeric = Enter your importedPlasticPackagingWeight using numbers" >> ../conf/messages.en
echo "importedPlasticPackagingWeight.error.required = Enter your importedPlasticPackagingWeight" >> ../conf/messages.en
echo "importedPlasticPackagingWeight.error.wholeNumber = Enter your importedPlasticPackagingWeight using whole numbers" >> ../conf/messages.en
echo "importedPlasticPackagingWeight.error.outOfRange = ImportedPlasticPackagingWeight must be between {0} and {1}" >> ../conf/messages.en
echo "importedPlasticPackagingWeight.change.hidden = ImportedPlasticPackagingWeight" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryImportedPlasticPackagingWeightUserAnswersEntry: Arbitrary[(ImportedPlasticPackagingWeightPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ImportedPlasticPackagingWeightPage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryImportedPlasticPackagingWeightPage: Arbitrary[ImportedPlasticPackagingWeightPage.type] =";\
    print "    Arbitrary(ImportedPlasticPackagingWeightPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ImportedPlasticPackagingWeightPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration ImportedPlasticPackagingWeight completed"
