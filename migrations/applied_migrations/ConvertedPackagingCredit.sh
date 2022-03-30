#!/bin/bash

echo ""
echo "Applying migration ConvertedPackagingCredit"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /convertedPackagingCredit                  controllers.ConvertedPackagingCreditController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /convertedPackagingCredit                  controllers.ConvertedPackagingCreditController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeConvertedPackagingCredit                        controllers.ConvertedPackagingCreditController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeConvertedPackagingCredit                        controllers.ConvertedPackagingCreditController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "convertedPackagingCredit.title = ConvertedPackagingCredit" >> ../conf/messages.en
echo "convertedPackagingCredit.heading = ConvertedPackagingCredit" >> ../conf/messages.en
echo "convertedPackagingCredit.checkYourAnswersLabel = ConvertedPackagingCredit" >> ../conf/messages.en
echo "convertedPackagingCredit.error.nonNumeric = Enter your convertedPackagingCredit using numbers" >> ../conf/messages.en
echo "convertedPackagingCredit.error.required = Enter your convertedPackagingCredit" >> ../conf/messages.en
echo "convertedPackagingCredit.error.wholeNumber = Enter your convertedPackagingCredit using whole numbers" >> ../conf/messages.en
echo "convertedPackagingCredit.error.outOfRange = ConvertedPackagingCredit must be between {0} and {1}" >> ../conf/messages.en
echo "convertedPackagingCredit.change.hidden = ConvertedPackagingCredit" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryConvertedPackagingCreditUserAnswersEntry: Arbitrary[(ConvertedPackagingCreditPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ConvertedPackagingCreditPage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryConvertedPackagingCreditPage: Arbitrary[ConvertedPackagingCreditPage.type] =";\
    print "    Arbitrary(ConvertedPackagingCreditPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ConvertedPackagingCreditPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration ConvertedPackagingCredit completed"
