#!/bin/bash

echo ""
echo "Applying migration RecycledPlasticPackagingWeight"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /recycledPlasticPackagingWeight                  controllers.RecycledPlasticPackagingWeightController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /recycledPlasticPackagingWeight                  controllers.RecycledPlasticPackagingWeightController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeRecycledPlasticPackagingWeight                        controllers.RecycledPlasticPackagingWeightController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeRecycledPlasticPackagingWeight                        controllers.RecycledPlasticPackagingWeightController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "recycledPlasticPackagingWeight.title = RecycledPlasticPackagingWeight" >> ../conf/messages.en
echo "recycledPlasticPackagingWeight.heading = RecycledPlasticPackagingWeight" >> ../conf/messages.en
echo "recycledPlasticPackagingWeight.checkYourAnswersLabel = RecycledPlasticPackagingWeight" >> ../conf/messages.en
echo "recycledPlasticPackagingWeight.error.nonNumeric = Enter your recycledPlasticPackagingWeight using numbers" >> ../conf/messages.en
echo "recycledPlasticPackagingWeight.error.required = Enter your recycledPlasticPackagingWeight" >> ../conf/messages.en
echo "recycledPlasticPackagingWeight.error.wholeNumber = Enter your recycledPlasticPackagingWeight using whole numbers" >> ../conf/messages.en
echo "recycledPlasticPackagingWeight.error.outOfRange = RecycledPlasticPackagingWeight must be between {0} and {1}" >> ../conf/messages.en
echo "recycledPlasticPackagingWeight.change.hidden = RecycledPlasticPackagingWeight" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryRecycledPlasticPackagingWeightUserAnswersEntry: Arbitrary[(RecycledPlasticPackagingWeightPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[RecycledPlasticPackagingWeightPage.type]";\
    print "        value <- arbitrary[Int].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryRecycledPlasticPackagingWeightPage: Arbitrary[RecycledPlasticPackagingWeightPage.type] =";\
    print "    Arbitrary(RecycledPlasticPackagingWeightPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(RecycledPlasticPackagingWeightPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration RecycledPlasticPackagingWeight completed"
