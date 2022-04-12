#!/bin/bash

echo ""
echo "Applying migration SubmittedReturns"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /submittedReturns                       controllers.SubmittedReturnsController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "submittedReturns.title = submittedReturns" >> ../conf/messages.en
echo "submittedReturns.heading = submittedReturns" >> ../conf/messages.en

echo "Migration SubmittedReturns completed"
