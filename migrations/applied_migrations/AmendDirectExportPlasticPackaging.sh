#!/bin/bash

echo ""
echo "Applying migration AmendDirectExportPlasticPackaging"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /amendDirectExportPlasticPackaging                  controllers.AmendDirectExportPlasticPackagingController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /amendDirectExportPlasticPackaging                  controllers.AmendDirectExportPlasticPackagingController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAmendDirectExportPlasticPackaging                        controllers.AmendDirectExportPlasticPackagingController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAmendDirectExportPlasticPackaging                        controllers.AmendDirectExportPlasticPackagingController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "amendDirectExportPlasticPackaging.title = AmendDirectExportPlasticPackaging" >> ../conf/messages.en
echo "amendDirectExportPlasticPackaging.heading = AmendDirectExportPlasticPackaging" >> ../conf/messages.en
echo "amendDirectExportPlasticPackaging.checkYourAnswersLabel = AmendDirectExportPlasticPackaging" >> ../conf/messages.en
echo "amendDirectExportPlasticPackaging.error.nonNumeric = Enter your amendDirectExportPlasticPackaging using numbers" >> ../conf/messages.en
echo "amendDirectExportPlasticPackaging.error.required = Enter your amendDirectExportPlasticPackaging" >> ../conf/messages.en
echo "amendDirectExportPlasticPackaging.error.wholeNumber = Enter your amendDirectExportPlasticPackaging using whole numbers" >> ../conf/messages.en
echo "amendDirectExportPlasticPackaging.error.outOfRange = AmendDirectExportPlasticPackaging must be between {0} and {1}" >> ../conf/messages.en
echo "amendDirectExportPlasticPackaging.change.hidden = AmendDirectExportPlasticPackaging" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendDirectExportPlasticPackagingUserAnswersEntry: Arbitrary[(AmendDirectExportPlasticPackagingPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AmendDirectExportPlasticPackagingPage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendDirectExportPlasticPackagingPage: Arbitrary[AmendDirectExportPlasticPackagingPage.type] =";\
    print "    Arbitrary(AmendDirectExportPlasticPackagingPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AmendDirectExportPlasticPackagingPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration AmendDirectExportPlasticPackaging completed"
