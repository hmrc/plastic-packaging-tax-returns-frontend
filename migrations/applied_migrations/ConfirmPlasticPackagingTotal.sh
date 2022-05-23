#!/bin/bash

echo ""
echo "Applying migration ConfirmPlasticPackagingTotal"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /confirm-plastic-packaging-total                       controllers.ConfirmPlasticPackagingTotalController.onPageLoad()" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "confirmPlasticPackagingTotal.title = confirmPlasticPackagingTotal" >> ../conf/messages.en
echo "confirmPlasticPackagingTotal.heading = confirmPlasticPackagingTotal" >> ../conf/messages.en

echo "Migration ConfirmPlasticPackagingTotal completed"
