#!/bin/bash

echo ""
echo "Applying migration ViewReturnSummary"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /viewReturnSummary                       controllers.ViewReturnSummaryController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "viewReturnSummary.title = viewReturnSummary" >> ../conf/messages.en
echo "viewReturnSummary.heading = viewReturnSummary" >> ../conf/messages.en

echo "Migration ViewReturnSummary completed"
