#!/bin/bash

echo ""
echo "Applying migration ImportedPlasticPackaging"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /importedPlasticPackaging                        controllers.ImportedPlasticPackagingController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /importedPlasticPackaging                        controllers.ImportedPlasticPackagingController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeImportedPlasticPackaging                  controllers.ImportedPlasticPackagingController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeImportedPlasticPackaging                  controllers.ImportedPlasticPackagingController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "importedPlasticPackaging.title = importedPlasticPackaging" >> ../conf/messages.en
echo "importedPlasticPackaging.heading = importedPlasticPackaging" >> ../conf/messages.en
echo "importedPlasticPackaging.checkYourAnswersLabel = importedPlasticPackaging" >> ../conf/messages.en
echo "importedPlasticPackaging.error.required = Select yes if importedPlasticPackaging" >> ../conf/messages.en
echo "importedPlasticPackaging.change.hidden = ImportedPlasticPackaging" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryImportedPlasticPackagingUserAnswersEntry: Arbitrary[(ImportedPlasticPackagingPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ImportedPlasticPackagingPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryImportedPlasticPackagingPage: Arbitrary[ImportedPlasticPackagingPage.type] =";\
    print "    Arbitrary(ImportedPlasticPackagingPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ImportedPlasticPackagingPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration ImportedPlasticPackaging completed"
