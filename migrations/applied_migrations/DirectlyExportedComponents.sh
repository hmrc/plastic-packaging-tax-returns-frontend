#!/bin/bash

echo ""
echo "Applying migration DirectlyExportedComponents"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /directly-exported-components                        controllers.DirectlyExportedComponentsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /directly-exported-components                        controllers.DirectlyExportedComponentsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeDirectlyExportedComponents                  controllers.DirectlyExportedComponentsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeDirectlyExportedComponents                  controllers.DirectlyExportedComponentsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "directlyExportedComponents.title = directlyExportedComponents" >> ../conf/messages.en
echo "directlyExportedComponents.heading = directlyExportedComponents" >> ../conf/messages.en
echo "directlyExportedComponents.checkYourAnswersLabel = directlyExportedComponents" >> ../conf/messages.en
echo "directlyExportedComponents.error.required = Select yes if directlyExportedComponents" >> ../conf/messages.en
echo "directlyExportedComponents.change.hidden = DirectlyExportedComponents" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDirectlyExportedComponentsUserAnswersEntry: Arbitrary[(DirectlyExportedComponentsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[DirectlyExportedComponentsPage.type]";\
    print "        value <- arbitrary[Boolean].map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryDirectlyExportedComponentsPage: Arbitrary[DirectlyExportedComponentsPage.type] =";\
    print "    Arbitrary(DirectlyExportedComponentsPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(DirectlyExportedComponentsPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration DirectlyExportedComponents completed"
