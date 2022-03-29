#!/bin/bash

echo ""
echo "Applying migration endAreYouSure"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /endAreYouSure                        controllers.endAreYouSureController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /endAreYouSure                        controllers.endAreYouSureController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeendAreYouSure                  controllers.endAreYouSureController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeendAreYouSure                  controllers.endAreYouSureController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "endAreYouSure.title = endAreYouSure" >> ../conf/messages.en
echo "endAreYouSure.heading = endAreYouSure" >> ../conf/messages.en
echo "endAreYouSure.checkYourAnswersLabel = endAreYouSure" >> ../conf/messages.en
echo "endAreYouSure.error.required = Select yes if endAreYouSure" >> ../conf/messages.en
echo "endAreYouSure.change.hidden = endAreYouSure" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryendAreYouSureUserAnswersEntry: Arbitrary[(endAreYouSurePage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[endAreYouSurePage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryendAreYouSurePage: Arbitrary[endAreYouSurePage.type] =";\
    print "    Arbitrary(endAreYouSurePage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(endAreYouSurePage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration endAreYouSure completed"
