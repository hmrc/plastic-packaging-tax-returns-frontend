// ==UserScript==
// @name         PPT Returns AutoComplete
// @namespace    http://tampermonkey.net/
// @version      5.2
// @description
// @author       pmonteiro
// @match        http*://*/submit-return-for-plastic-packaging-tax*
// @grant GM_setValue
// @grant GM_getValue
// @updateURL    https://raw.githubusercontent.com/hmrc/plastic-packaging-tax-returns-frontend/master/tampermonkey/PPT_Returns_AutoComplete.js
// ==/UserScript==

/*eslint no-undef: "error"*/

(function() {
    'use strict'
    document.body.appendChild(setup())
})()

function setup() {
    var panel = document.createElement('div')

    panel.appendChild(createQuickButton())

    return panel
}

function createQuickButton() {

    let button = document.createElement('button')
    button.id='quickSubmit'

    if (!!document.getElementById('global-header')) {
        button.classList.add('button-start', 'govuk-!-display-none-print')
    } else {
        button.classList.add('govuk-button','govuk-!-display-none-print')
    }

    button.style.position = 'absolute'
    button.style.top = '50px'
    button.innerHTML = 'Quick Submit'
    button.onclick = () => completeJourney()

    return button
}

const currentPageIs = (path) => {
    if(path.includes("*")) {
        let matches = window.location.pathname.match(path)
        return matches && window.location.pathname.endsWith(path.slice(-5))
    } else {
        return path === window.location.pathname
    }
}

/*########################     PPT REGISTRATION PAGES     ########################## */
const startPage = () => {
    if (currentPageIs('/submit-return-for-plastic-packaging-tax/submit-return')) {

        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const manufacturedWeightPage = () => {
    if (currentPageIs('/submit-return-for-plastic-packaging-tax/manufactured-plastic-packaging-weight')) {

        document.getElementById('totalKg').value = '10'
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const importedWeightPage = () => {
    if (currentPageIs('/submit-return-for-plastic-packaging-tax/imported-plastic-packaging-weight')) {

        document.getElementById('totalKg').value = '20'
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const humanMedicinesWeightPage = () => {
    if (currentPageIs('/submit-return-for-plastic-packaging-tax/human-medicines-packaging-weight')) {

        document.getElementById('totalKg').value = '1'
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const exportedPackagingWeightPage = () => {
    if (currentPageIs('/submit-return-for-plastic-packaging-tax/exported-plastic-packaging-weight')) {

        document.getElementById('totalKg').value = '2'
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const convertedPackagingCreditPage = () => {
    if (currentPageIs('/submit-return-for-plastic-packaging-tax/converted-plastic-packaging-credit')) {

        document.getElementById('totalInPence').value = '200.34'
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const recycledWeightPage = () => {
    if (currentPageIs('/submit-return-for-plastic-packaging-tax/recycled-plastic-packaging')) {

        document.getElementById('totalKg').value = '10'
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const reviewReturn = () => {
     if (currentPageIs('/submit-return-for-plastic-packaging-tax/check-your-return')) {

         document.getElementsByClassName('govuk-button')[0].click()
     }
}

const confirmationPage = () => {
     if (currentPageIs('/submit-return-for-plastic-packaging-tax/return-complete')) {

         document.getElementsByClassName('govuk-link')[5].click()
     }
}

/*########################     MAIN FUNCTION     ########################## */
const completeJourney = () => {

    startPage()
    manufacturedWeightPage()
    importedWeightPage()
    humanMedicinesWeightPage()
    exportedPackagingWeightPage()
    convertedPackagingCreditPage()
    recycledWeightPage()
    reviewReturn()
    confirmationPage()

}