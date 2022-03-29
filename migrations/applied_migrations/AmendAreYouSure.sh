#!/bin/bash

echo ""
echo "Applying migration AmendAreYouSure"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /amendAreYouSure                        controllers.AmendAreYouSureController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /amendAreYouSure                        controllers.AmendAreYouSureController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAmendAreYouSure                  controllers.AmendAreYouSureController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAmendAreYouSure                  controllers.AmendAreYouSureController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "amendAreYouSure.title = amendAreYouSure" >> ../conf/messages.en
echo "amendAreYouSure.heading = amendAreYouSure" >> ../conf/messages.en
echo "amendAreYouSure.checkYourAnswersLabel = amendAreYouSure" >> ../conf/messages.en
echo "amendAreYouSure.error.required = Select yes if amendAreYouSure" >> ../conf/messages.en
echo "amendAreYouSure.change.hidden = AmendAreYouSure" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendAreYouSureUserAnswersEntry: Arbitrary[(AmendAreYouSurePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AmendAreYouSurePage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendAreYouSurePage: Arbitrary[AmendAreYouSurePage.type] =";\
    print "    Arbitrary(AmendAreYouSurePage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AmendAreYouSurePage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration AmendAreYouSure completed"
