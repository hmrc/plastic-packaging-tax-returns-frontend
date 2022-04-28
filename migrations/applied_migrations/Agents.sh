#!/bin/bash

echo ""
echo "Applying migration Agents"

echo "Adding routes to conf/app.routes"

echo "" >> ../conf/app.routes
echo "GET        /agents                        controllers.AgentsController.onPageLoad(mode: Mode = NormalMode)" >> ../conf/app.routes
echo "POST       /agents                        controllers.AgentsController.onSubmit(mode: Mode = NormalMode)" >> ../conf/app.routes

echo "GET        /changeAgents                  controllers.AgentsController.onPageLoad(mode: Mode = CheckMode)" >> ../conf/app.routes
echo "POST       /changeAgents                  controllers.AgentsController.onSubmit(mode: Mode = CheckMode)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "agents.title = agents" >> ../conf/messages.en
echo "agents.heading = agents" >> ../conf/messages.en
echo "agents.checkYourAnswersLabel = agents" >> ../conf/messages.en
echo "agents.error.required = Enter agents" >> ../conf/messages.en
echo "agents.error.length = Agents must be 15 characters or less" >> ../conf/messages.en
echo "agents.change.hidden = Agents" >> ../conf/messages.en

echo "Adding to UserAnswersEntryGenerators"
awk '/trait UserAnswersEntryGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAgentsUserAnswersEntry: Arbitrary[(AgentsPage.type, JsValue)] =";\
    print "    Arbitrary {";\
    print "      for {";\
    print "        page  <- arbitrary[AgentsPage.type]";\
    print "        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))";\
    print "      } yield (page, value)";\
    print "    }";\
    next }1' ../test-utils/generators/UserAnswersEntryGenerators.scala > tmp && mv tmp ../test-utils/generators/UserAnswersEntryGenerators.scala

echo "Adding to PageGenerators"
awk '/trait PageGenerators/ {\
    print;\
    print "";\
    print "  implicit lazy val arbitraryAgentsPage: Arbitrary[AgentsPage.type] =";\
    print "    Arbitrary(AgentsPage)";\
    next }1' ../test-utils/generators/PageGenerators.scala > tmp && mv tmp ../test-utils/generators/PageGenerators.scala

echo "Adding to UserAnswersGenerator"
awk '/val generators/ {\
    print;\
    print "    arbitrary[(AgentsPage.type, JsValue)] ::";\
    next }1' ../test-utils/generators/UserAnswersGenerator.scala > tmp && mv tmp ../test-utils/generators/UserAnswersGenerator.scala

echo "Migration Agents completed"
