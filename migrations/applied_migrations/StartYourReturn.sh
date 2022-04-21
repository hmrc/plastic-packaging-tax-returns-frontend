#!/bin/bash

echo ""
echo "Applying migration StartYourReturn"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /startYourReturn                        controllers.StartYourReturnController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /startYourReturn                        controllers.StartYourReturnController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeStartYourReturn                  controllers.StartYourReturnController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeStartYourReturn                  controllers.StartYourReturnController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "startYourReturn.title = startYourReturn" >> ../conf/messages.en
echo "startYourReturn.heading = startYourReturn" >> ../conf/messages.en
echo "startYourReturn.checkYourAnswersLabel = startYourReturn" >> ../conf/messages.en
echo "startYourReturn.error.required = Select yes if startYourReturn" >> ../conf/messages.en
echo "startYourReturn.change.hidden = StartYourReturn" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryStartYourReturnUserAnswersEntry: Arbitrary[(StartYourReturnPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[StartYourReturnPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryStartYourReturnPage: Arbitrary[StartYourReturnPage.type] =";\
    print "    Arbitrary(StartYourReturnPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(StartYourReturnPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration StartYourReturn completed"
