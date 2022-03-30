#!/bin/bash

echo ""
echo "Applying migration ManufacturedPlasticPackaging"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /manufacturedPlasticPackaging                        controllers.ManufacturedPlasticPackagingController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /manufacturedPlasticPackaging                        controllers.ManufacturedPlasticPackagingController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeManufacturedPlasticPackaging                  controllers.ManufacturedPlasticPackagingController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeManufacturedPlasticPackaging                  controllers.ManufacturedPlasticPackagingController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "manufacturedPlasticPackaging.title = manufacturedPlasticPackaging" >> ../conf/messages.en
echo "manufacturedPlasticPackaging.heading = manufacturedPlasticPackaging" >> ../conf/messages.en
echo "manufacturedPlasticPackaging.checkYourAnswersLabel = manufacturedPlasticPackaging" >> ../conf/messages.en
echo "manufacturedPlasticPackaging.error.required = Select yes if manufacturedPlasticPackaging" >> ../conf/messages.en
echo "manufacturedPlasticPackaging.change.hidden = ManufacturedPlasticPackaging" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryManufacturedPlasticPackagingUserAnswersEntry: Arbitrary[(ManufacturedPlasticPackagingPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[ManufacturedPlasticPackagingPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryManufacturedPlasticPackagingPage: Arbitrary[ManufacturedPlasticPackagingPage.type] =";\
    print "    Arbitrary(ManufacturedPlasticPackagingPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(ManufacturedPlasticPackagingPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration ManufacturedPlasticPackaging completed"
