#!/bin/bash

echo ""
echo "Applying migration AmendManufacturedPlasticPackaging"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /amendManufacturedPlasticPackaging                  controllers.AmendManufacturedPlasticPackagingController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /amendManufacturedPlasticPackaging                  controllers.AmendManufacturedPlasticPackagingController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAmendManufacturedPlasticPackaging                        controllers.AmendManufacturedPlasticPackagingController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAmendManufacturedPlasticPackaging                        controllers.AmendManufacturedPlasticPackagingController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "amendManufacturedPlasticPackaging.title = AmendManufacturedPlasticPackaging" >> ../conf/messages.en
echo "amendManufacturedPlasticPackaging.heading = AmendManufacturedPlasticPackaging" >> ../conf/messages.en
echo "amendManufacturedPlasticPackaging.checkYourAnswersLabel = AmendManufacturedPlasticPackaging" >> ../conf/messages.en
echo "amendManufacturedPlasticPackaging.error.nonNumeric = Enter your amendManufacturedPlasticPackaging using numbers" >> ../conf/messages.en
echo "amendManufacturedPlasticPackaging.error.required = Enter your amendManufacturedPlasticPackaging" >> ../conf/messages.en
echo "amendManufacturedPlasticPackaging.error.wholeNumber = Enter your amendManufacturedPlasticPackaging using whole numbers" >> ../conf/messages.en
echo "amendManufacturedPlasticPackaging.error.outOfRange = AmendManufacturedPlasticPackaging must be between {0} and {1}" >> ../conf/messages.en
echo "amendManufacturedPlasticPackaging.change.hidden = AmendManufacturedPlasticPackaging" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendManufacturedPlasticPackagingUserAnswersEntry: Arbitrary[(AmendManufacturedPlasticPackagingPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AmendManufacturedPlasticPackagingPage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAmendManufacturedPlasticPackagingPage: Arbitrary[AmendManufacturedPlasticPackagingPage.type] =";\
    print "    Arbitrary(AmendManufacturedPlasticPackagingPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AmendManufacturedPlasticPackagingPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration AmendManufacturedPlasticPackaging completed"
