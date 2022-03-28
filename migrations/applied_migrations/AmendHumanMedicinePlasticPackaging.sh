#!/bin/bash

echo ""
echo "Applying migration AmendHumanMedicinePlasticPackaging"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /amendHumanMedicinePlasticPackaging                  controllers.AmendHumanMedicinePlasticPackagingController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /amendHumanMedicinePlasticPackaging                  controllers.AmendHumanMedicinePlasticPackagingController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAmendHumanMedicinePlasticPackaging                        controllers.AmendHumanMedicinePlasticPackagingController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAmendHumanMedicinePlasticPackaging                        controllers.AmendHumanMedicinePlasticPackagingController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "amendHumanMedicinePlasticPackaging.title = AmendHumanMedicinePlasticPackaging" >> ../conf/messages.en
echo "amendHumanMedicinePlasticPackaging.heading = AmendHumanMedicinePlasticPackaging" >> ../conf/messages.en
echo "amendHumanMedicinePlasticPackaging.checkYourAnswersLabel = AmendHumanMedicinePlasticPackaging" >> ../conf/messages.en
echo "amendHumanMedicinePlasticPackaging.error.nonNumeric = Enter your amendHumanMedicinePlasticPackaging using numbers" >> ../conf/messages.en
echo "amendHumanMedicinePlasticPackaging.error.required = Enter your amendHumanMedicinePlasticPackaging" >> ../conf/messages.en
echo "amendHumanMedicinePlasticPackaging.error.wholeNumber = Enter your amendHumanMedicinePlasticPackaging using whole numbers" >> ../conf/messages.en
echo "amendHumanMedicinePlasticPackaging.error.outOfRange = AmendHumanMedicinePlasticPackaging must be between {0} and {1}" >> ../conf/messages.en
echo "amendHumanMedicinePlasticPackaging.change.hidden = AmendHumanMedicinePlasticPackaging" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendHumanMedicinePlasticPackagingUserAnswersEntry: Arbitrary[(AmendHumanMedicinePlasticPackagingPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AmendHumanMedicinePlasticPackagingPage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendHumanMedicinePlasticPackagingPage: Arbitrary[AmendHumanMedicinePlasticPackagingPage.type] =";\
    print "    Arbitrary(AmendHumanMedicinePlasticPackagingPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AmendHumanMedicinePlasticPackagingPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration AmendHumanMedicinePlasticPackaging completed"
