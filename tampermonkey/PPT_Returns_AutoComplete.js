// ==UserScript==
// @name         PPT Returns AutoComplete
// @namespace    http://tampermonkey.net/
// @version      1.0
// @description
// @author       pmonteiro
// @match        http*://*/plastic-packaging-tax/returns*
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
    if (currentPageIs('/plastic-packaging-tax/returns/submit-return')) {

        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const manufacturedWeightPage = () => {
    if (currentPageIs('/plastic-packaging-tax/returns/manufactured-plastic-packaging-weight')) {

        document.getElementById('totalKg').value = '10'
        document.getElementById('totalKgBelowThreshold').value = '5'
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const importedWeightPage = () => {
    if (currentPageIs('/plastic-packaging-tax/returns/imported-plastic-packaging-weight')) {

        document.getElementById('totalKg').value = '20'
        document.getElementById('totalKgBelowThreshold').value = '8'
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const humanMedicinesWeightPage = () => {
    if (currentPageIs('/plastic-packaging-tax/returns/human-medicines-plastic-packaging-weight')) {

        document.getElementById('totalKg').value = '1'
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const exportedPackagingWeightPage = () => {
    if (currentPageIs('/plastic-packaging-tax/returns/exported-plastic-packaging-weight')) {

        document.getElementById('totalKg').value = '2'
        document.getElementById('totalValueForCredit').value = '3'
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const convertedPackagingCreditPage = () => {
    if (currentPageIs('/plastic-packaging-tax/returns/converted-plastic-packaging-credit')) {

        document.getElementById('totalInPence').value = '200.34'
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const reviewReturn = () => {
     if (currentPageIs('/plastic-packaging-tax/returns/check-your-return')) {

         document.getElementsByClassName('govuk-button')[0].click()
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
    reviewReturn()

}