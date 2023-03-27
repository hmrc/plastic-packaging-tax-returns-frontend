// ==UserScript==
// @name         PPT Returns AutoComplete
// @namespace    http://tampermonkey.net/
// @version      5.7
// @description
// @author       pmonteiro
// @match        http*://*/plastic-packaging-tax/*
// @include      http*://*/register-for-plastic-packaging-tax*
// @grant GM_setValue
// @grant GM_getValue
// @updateURL    https://raw.githubusercontent.com/hmrc/plastic-packaging-tax-returns-frontend/master/tampermonkey/PPT_Returns_AutoComplete.js
// ==/UserScript==

/*eslint no-undef: "error"*/

(function() {
    'use strict'
    document.body.appendChild(setup())

    setTimeout(function() {
        if(getAutocomplete()){
            completeJourney(false)
        }
    }, 100)
})()

function setAutocomplete(choice) {
    GM_setValue("autoComplete", choice);
}

function getAutocomplete() {
    return GM_getValue("autoComplete");
}

function optionSelected(option, value) {
    return GM_getValue(option) == value;
}

function setup() {
    var panel = document.createElement('div')
    panel.style.position = 'absolute'
    panel.style.top = '50px'
    panel.style.lineHeight = '200%'

    panel.appendChild(createQuickButton())
    panel.appendChild(createAutoCompleteCheckbox())
    panel.appendChild(createDropDown("Journey", ["Return","Return with credit","Amend Return","Deregister","Change group lead"]))

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

    button.innerHTML = 'Quick Submit'
    button.onclick = () => completeJourney(true)

    return button
}

function createDropDown(name, options) {
    var panel = document.createElement("div");

    var id = "my" + name;

    var label = document.createElement("label");
    label.innerText = name + ":";
    label.setAttribute("for", id);
    panel.appendChild(label);

    // create and append select list
    var selectList = document.createElement("select");
    selectList.id = id;
    selectList.className = "govuk-!-display-none-print";
    panel.appendChild(selectList);

    // create and append the options
    options.forEach(item => {
            var option = document.createElement("option");
            option.value = item;
            option.text = item;

            if(GM_getValue(name) == item) option.selected = true;

            selectList.appendChild(option);
        }
    )

    selectList.onchange = function (e) {
        GM_setValue(name, this.value);
    };

    return panel;
}

function createAutoCompleteCheckbox() {

    let chkBox = document.createElement('input')
    chkBox.id='autoComplete'
    chkBox.type = "checkbox"
    chkBox.checked = getAutocomplete()

    chkBox.onchange = function (e) { setAutocomplete(this.checked); };

    let panel = document.createElement("div");
    panel.appendChild(chkBox);
    let label = document.createElement("label");
    label.innerText = "Auto complete";
    label.setAttribute("for", "autoComplete");
    panel.appendChild(label);

    return panel
}

const currentPageIs = (path) => {
    if(path.includes("*")) {
        let matches = window.location.pathname.match(path)
        return matches
    } else {
        return path === window.location.pathname
    }
}

/*########################     PPT RETURNS PAGES     ########################## */
const startPage = () => {
    if (currentPageIs('/plastic-packaging-tax/account')) {
        if(optionSelected("Journey", "Return")||optionSelected("Journey", "Return with credit")) {
           document.getElementById('start-date-return-link').click()
        }
        else if (optionSelected("Journey", "Amend Return")) {
           document.getElementById('view-submitted-returns-link').click()
        }
        else if (optionSelected("Journey", "Deregister")) {
           document.getElementById('amend-deregister').click()
        }
        else if (optionSelected("Journey", "Change group lead")) {
           document.getElementById('amend-group').click()
        }
    }
}

const returnAccountingPeriod = () => {
    if (currentPageIs('/plastic-packaging-tax/return-accounting-period')) {

        document.getElementById('value').checked = true
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const submitReturnOrClaimCredit = () => {
    if (currentPageIs('/plastic-packaging-tax/submit-return-or-claim-credit')) {

        if(optionSelected("Journey", "Return")){
           document.getElementById('radio-just-return').checked = true
        }
        else if(optionSelected("Journey", "Return with credit")){
            document.getElementById('value').checked = true
        }

        document.getElementsByClassName('govuk-button')[0].click()
    }
}

/* ####################### Returns credit pages */

const creditForExported = () => {
     if (currentPageIs('/plastic-packaging-tax/credit-for-exported')) {
         document.getElementById('answer').click()
         document.getElementById('exported-credits-weight').value = '10'
         document.getElementsByClassName('govuk-button')[0].click()
     }
}

const creditForConverted = () => {
     if (currentPageIs('/plastic-packaging-tax/credit-for-converted')) {
         document.getElementById('answer').click()
         document.getElementById('converted-credits-weight').value = '10'
         document.getElementsByClassName('govuk-button')[0].click()
     }
}

const confirmOrCorrectCredit = () => {
         if (currentPageIs('/plastic-packaging-tax/confirm-or-correct-credit')) {
         document.getElementsByClassName('govuk-button')[0].click()
     }
}

const startReturn = () => {
         if (currentPageIs('/plastic-packaging-tax/start-return')) {
         document.getElementsByClassName('govuk-button')[0].click()
     }
}

/* ####################### Returns pages */

const manufacturedComponentsPage = () => {
    if (currentPageIs('/plastic-packaging-tax/manufactured-components')) {

        document.getElementById('value').click()
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const manufacturedComponentsWeightPage = () => {
    if (currentPageIs('/plastic-packaging-tax/manufactured-weight')) {

        document.getElementById('value').value = '1000'
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const importedComponentsPage = () => {
    if (currentPageIs('/plastic-packaging-tax/imported-components')) {

        document.getElementById('value').click()
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const importedComponentsWeightPage = () => {
    if (currentPageIs('/plastic-packaging-tax/imported-weight')) {

        document.getElementById('value').value = '1000'
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const confirmPlasticPackagingTotalPage = () => {
    if (currentPageIs('/plastic-packaging-tax/confirm-plastic-packaging-total')) {

        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const directlyExportedComponentsPage = () => {
    if (currentPageIs('/plastic-packaging-tax/directly-exported-components')) {

        document.getElementById('value').click()
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const directlyExportedComponentsWeightPage = () => {
    if (currentPageIs('/plastic-packaging-tax/directly-exported-weight')) {

        document.getElementById('value').value = '100'
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const exportedComponentsByAnotherBusinessPage = () => {
    if (currentPageIs('/plastic-packaging-tax/exported-by-another-business')) {

        document.getElementById('value').click()
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const exportedComponentsByAnotherBusinessWeightPage = () => {
    if (currentPageIs('/plastic-packaging-tax/another-business-exported-weight')) {

        document.getElementById('value').value = '150'
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const nonExportedMedicinesPage = () => {
    if (currentPageIs('/plastic-packaging-tax/non-exported-medicines-packaging')) {

        document.getElementById('value').click()
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const nonExportedMedicinesWeightPage = () => {
    if (currentPageIs('/plastic-packaging-tax/non-exported-medicines-packaging-weight')) {

        document.getElementById('value').value = '50'
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const nonExportedRecycledPage = () => {
    if (currentPageIs('/plastic-packaging-tax/non-exported-recycled')) {

        document.getElementById('value').click()
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

const nonExportedRecycledWeightPage = () => {
    if (currentPageIs('/plastic-packaging-tax/non-exported-recycled-weight')) {

        document.getElementById('value').value = '75'
        document.getElementsByClassName('govuk-button')[0].click()
    }
}

/* ####################### Submit return pages */

const reviewReturn = () => {
     if (currentPageIs('/plastic-packaging-tax/returns-check-your-answers')) {

         document.getElementsByClassName('govuk-button')[0].click()
     }
}

const confirmationPage = () => {
     if (currentPageIs('/plastic-packaging-tax/return-submitted/false')) {

         document.getElementById('account-link').click()
     }
}

/* ####################### Amend submitted returns pages */

const submittedReturns = () => {
     if (currentPageIs('/plastic-packaging-tax/submitted-returns')) {
         document.getElementsByClassName('govuk-body')[1].click()
     }
}

const existingReturn = () => {
     if (currentPageIs('/plastic-packaging-tax/viewReturnSummary/*')) {
         document.getElementById('amend-return').click()
     }
}

const amendReturn = () => {
     if (currentPageIs('/plastic-packaging-tax/amend-return')) {
         let noChange = document.querySelector('#main-content > div > div > table:nth-child(2) > tbody > tr:nth-child(1) > td:nth-child(3) > span').innerText
         if (noChange == 'Not yet amended') {
             document.getElementById('amend-manufacture-link').click()
         }
     }
}

const amendManufacturedWeight = () => {
     if (currentPageIs('/plastic-packaging-tax/amend-manufactured-weight')) {
         document.getElementById('value').value = '251'
         document.getElementsByClassName('govuk-button')[0].click()
     }
}

const returnAmended = () => {
     if (currentPageIs('/plastic-packaging-tax/return-amended')) {
         document.querySelector('#main-content > div > div > p:nth-child(5) > a').click()
     }
}

/*########################     MAIN FUNCTION     ########################## */
function completeJourney(manualJourney) {

    startPage()
    returnAccountingPeriod()
    submitReturnOrClaimCredit()
    manufacturedComponentsPage()
    manufacturedComponentsWeightPage()
    importedComponentsPage()
    importedComponentsWeightPage()
    confirmPlasticPackagingTotalPage()
    directlyExportedComponentsPage()
    directlyExportedComponentsWeightPage()
    exportedComponentsByAnotherBusinessPage()
    exportedComponentsByAnotherBusinessWeightPage()
    nonExportedMedicinesPage()
    nonExportedMedicinesWeightPage()
    nonExportedRecycledPage()
    nonExportedRecycledWeightPage()
    creditForExported()
    creditForConverted()
    confirmOrCorrectCredit()
    startReturn()
    submittedReturns()
    existingReturn()
    amendReturn()
    amendManufacturedWeight()
    deregister()
    if(manualJourney){
        reviewReturn()
    }
    confirmationPage()
    returnAmended()

}
