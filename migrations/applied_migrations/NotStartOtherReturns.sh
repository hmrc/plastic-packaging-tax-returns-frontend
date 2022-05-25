#!/bin/bash

echo ""
echo "Applying migration NotStartOtherReturns"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /notStartOtherReturns                       controllers.NotStartOtherReturnsController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "notStartOtherReturns.title = notStartOtherReturns" >> ../conf/messages.en
echo "notStartOtherReturns.heading = notStartOtherReturns" >> ../conf/messages.en

echo "Migration NotStartOtherReturns completed"
