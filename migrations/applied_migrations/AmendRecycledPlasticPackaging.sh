#!/bin/bash

echo ""
echo "Applying migration AmendRecycledPlasticPackaging"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /amendRecycledPlasticPackaging                  controllers.AmendRecycledPlasticPackagingController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /amendRecycledPlasticPackaging                  controllers.AmendRecycledPlasticPackagingController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAmendRecycledPlasticPackaging                        controllers.AmendRecycledPlasticPackagingController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAmendRecycledPlasticPackaging                        controllers.AmendRecycledPlasticPackagingController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "amendRecycledPlasticPackaging.title = AmendRecycledPlasticPackaging" >> ../conf/messages.en
echo "amendRecycledPlasticPackaging.heading = AmendRecycledPlasticPackaging" >> ../conf/messages.en
echo "amendRecycledPlasticPackaging.checkYourAnswersLabel = AmendRecycledPlasticPackaging" >> ../conf/messages.en
echo "amendRecycledPlasticPackaging.error.nonNumeric = Enter your amendRecycledPlasticPackaging using numbers" >> ../conf/messages.en
echo "amendRecycledPlasticPackaging.error.required = Enter your amendRecycledPlasticPackaging" >> ../conf/messages.en
echo "amendRecycledPlasticPackaging.error.wholeNumber = Enter your amendRecycledPlasticPackaging using whole numbers" >> ../conf/messages.en
echo "amendRecycledPlasticPackaging.error.outOfRange = AmendRecycledPlasticPackaging must be between {0} and {1}" >> ../conf/messages.en
echo "amendRecycledPlasticPackaging.change.hidden = AmendRecycledPlasticPackaging" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendRecycledPlasticPackagingUserAnswersEntry: Arbitrary[(AmendRecycledPlasticPackagingPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AmendRecycledPlasticPackagingPage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendRecycledPlasticPackagingPage: Arbitrary[AmendRecycledPlasticPackagingPage.type] =";\
    print "    Arbitrary(AmendRecycledPlasticPackagingPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AmendRecycledPlasticPackagingPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration AmendRecycledPlasticPackaging completed"
