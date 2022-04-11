#!/bin/bash

echo ""
echo "Applying migration Deregistered"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /deregistered                       controllers.DeregisteredController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "deregistered.title = deregistered" >> ../conf/messages.en
echo "deregistered.heading = deregistered" >> ../conf/messages.en

echo "Migration Deregistered completed"
