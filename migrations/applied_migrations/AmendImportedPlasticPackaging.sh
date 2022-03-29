#!/bin/bash

echo ""
echo "Applying migration AmendImportedPlasticPackaging"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /amendImportedPlasticPackaging                  controllers.AmendImportedPlasticPackagingController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /amendImportedPlasticPackaging                  controllers.AmendImportedPlasticPackagingController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAmendImportedPlasticPackaging                        controllers.AmendImportedPlasticPackagingController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAmendImportedPlasticPackaging                        controllers.AmendImportedPlasticPackagingController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "amendImportedPlasticPackaging.title = AmendImportedPlasticPackaging" >> ../conf/messages.en
echo "amendImportedPlasticPackaging.heading = AmendImportedPlasticPackaging" >> ../conf/messages.en
echo "amendImportedPlasticPackaging.checkYourAnswersLabel = AmendImportedPlasticPackaging" >> ../conf/messages.en
echo "amendImportedPlasticPackaging.error.nonNumeric = Enter your amendImportedPlasticPackaging using numbers" >> ../conf/messages.en
echo "amendImportedPlasticPackaging.error.required = Enter your amendImportedPlasticPackaging" >> ../conf/messages.en
echo "amendImportedPlasticPackaging.error.wholeNumber = Enter your amendImportedPlasticPackaging using whole numbers" >> ../conf/messages.en
echo "amendImportedPlasticPackaging.error.outOfRange = AmendImportedPlasticPackaging must be between {0} and {1}" >> ../conf/messages.en
echo "amendImportedPlasticPackaging.change.hidden = AmendImportedPlasticPackaging" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendImportedPlasticPackagingUserAnswersEntry: Arbitrary[(AmendImportedPlasticPackagingPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AmendImportedPlasticPackagingPage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendImportedPlasticPackagingPage: Arbitrary[AmendImportedPlasticPackagingPage.type] =";\
    print "    Arbitrary(AmendImportedPlasticPackagingPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AmendImportedPlasticPackagingPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration AmendImportedPlasticPackaging completed"
