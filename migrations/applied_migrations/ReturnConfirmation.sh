#!/bin/bash

echo ""
echo "Applying migration ReturnConfirmation"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /returnConfirmation                       controllers.ReturnConfirmationController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "returnConfirmation.title = returnConfirmation" >> ../conf/messages.en
echo "returnConfirmation.heading = returnConfirmation" >> ../conf/messages.en

echo "Migration ReturnConfirmation completed"
