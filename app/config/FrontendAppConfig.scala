/*
 * Copyright 2025 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.i18n.Lang
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration, val servicesConfig: ServicesConfig) {

  lazy val host: String      = configuration.get[String]("host")
  lazy val appName: String   = configuration.get[String]("appName")
  lazy val mfaUpliftUrl      = configuration.get[String]("urls.mfaUplift")
  lazy val serviceIdentifier = "plastic-packaging-tax"

  lazy private val contactHost = configuration.get[String]("contact-frontend.host")

  lazy val userResearchUrl = configuration.get[String]("urls.userResearchUrl")

  lazy val loginUrl: String   = configuration.get[String]("urls.login")
  lazy val signOutUrl: String = configuration.get[String]("urls.signOut")

  lazy val exitSurveyUrl: String = configuration.get[String]("urls.exitSurvey")
  lazy val signedOutUrl: String  = configuration.get[String]("urls.signedOut")

  def languageMap: Map[String, Lang] = Map("en" -> Lang("en"), "cy" -> Lang("cy"))

  def contactFrontEnd = contactHost

  lazy val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  lazy val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  lazy val cacheTtl: Int = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  lazy val pptServiceHost: String =
    servicesConfig.baseUrl("plastic-packaging-tax-returns")

  def returnUrl(relative: String) = s"$host$relative"

  private lazy val pptReturnsSubmissionUrl: String           = s"$pptServiceHost/returns-submission"
  private lazy val pptReturnsAmendUrl: String                = s"$pptServiceHost/returns-amend"
  def pptReturnsCalculationUrl(pptReference: String): String = s"$pptServiceHost/returns-calculate/$pptReference"
  def pptAmendsCalculationUrl(pptReference: String): String  = s"$pptServiceHost/amends-calculate/$pptReference"

  lazy val pptRegistrationFrontEnd =
    configuration.getOptional[String]("platform.frontend.host").getOrElse(
      servicesConfig.baseUrl("ppt-registration-frontend")
    )

  lazy val pptRegistrationInfoUrl: String = configuration.get[String]("urls.pptRegistrationsInfoLink")
  lazy val pptRegistrationUrl: String     = s"$pptRegistrationFrontEnd/register-for-plastic-packaging-tax/start"

  def pptReturnSubmissionUrl(pptReference: String): String =
    s"$pptReturnsSubmissionUrl/$pptReference"

  def pptReturnAmendUrl(pptReference: String): String =
    s"$pptReturnsAmendUrl/$pptReference"

  lazy val pptRegistrationAmendUrl =
    s"$pptRegistrationFrontEnd/register-for-plastic-packaging-tax/amend-registration"

  lazy val pptRegistrationManageGroupUrl =
    s"$pptRegistrationFrontEnd/register-for-plastic-packaging-tax/manage-group-members"

  lazy val pptRegistrationManagePartnersUrl =
    s"$pptRegistrationFrontEnd/register-for-plastic-packaging-tax/manage-partners"

  lazy val pptRegistrationDeregisterUrl =
    s"$pptRegistrationFrontEnd/register-for-plastic-packaging-tax/deregister"

  def pptCalculateCreditsUrl(pptReference: String): String =
    s"$pptServiceHost/credits/calculate/$pptReference"

  def pptAvailableCreditYearsUrl(pptReference: String): String =
    s"$pptServiceHost/credits/available-years/$pptReference"

  def pptSubscriptionUrl(pptReference: String): String =
    s"$pptServiceHost/subscriptions/$pptReference"

  def pptChangeGroupLeadUrl(pptReference: String): String =
    s"$pptServiceHost/change-group-lead/$pptReference"

  lazy val pptCompleteReturnGuidanceUrl: String =
    configuration.get[String]("urls.pptCompleteReturnGuidanceLink")

  def pptOpenObligationUrl(pptReference: String): String =
    s"$pptServiceHost/obligations/open/$pptReference"

  def pptFulfilledObligationUrl(pptReference: String): String =
    s"$pptServiceHost/obligations/fulfilled/$pptReference"

  def pptFinancialsUrl(pptReference: String): String =
    s"$pptServiceHost/financials/open/$pptReference"

  def pptDDInProgress(pptReference: String, periodKey: String): String =
    s"$pptServiceHost/financials/dd-in-progress/$pptReference/$periodKey"

  def makePaymentUrl: String =
    servicesConfig.baseUrl("pay-api") + "/pay-api/plastic-packaging-tax/journey/start"

  def pptCacheGetUrl(pptReference: String): String =
    s"$pptServiceHost/cache/get/$pptReference"

  def pptCacheSetUrl(pptReference: String): String =
    s"$pptServiceHost/cache/set/$pptReference"

  lazy val businessAccountUrl: String = configuration.get[String]("urls.businessAccount")

  def pptStartDirectDebit: String =
    s"${servicesConfig.baseUrl("direct-debit")}/direct-debit-backend/ppt-homepage/ppt/journey/start"

  def creditsGuidanceUrl: String =
    configuration.get[String]("urls.pptCreditsGuidanceLink")

  def recordsToKeepGuidanceUrl: String =
    configuration.get[String]("urls.recordsToKeepGuidanceLink")

  def addMemberToGroupUrl: String =
    configuration.get[String]("urls.addMemberToGroup")

  def substantialModificationGuidanceUrl =
    configuration.get[String]("urls.pptSubstantialModificationGuidanceLink")

  /** Override the current system data-time, for coding and testing. The system date-time is used if the config value is
    * missing or its value fails to parse.
    * @return
    *   - [[None]] if no date-time override config value is present
    *   - Some[ [[String]] ] if an override config value is present, needs to be a ISO_LOCAL_DATE_TIME serialised
    *     date-time for override to work
    * @example
    *   {{{"2023-03-31T23:59:59"}}}
    * @example
    *   {{{"2023-04-01T00:00:00"}}} sets the override
    * @example
    *   {{{"DO_NOT_OVERRIDE"}}}
    * @see
    *   [[java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME]]
    * @see
    *   [[util.EdgeOfSystem.localDateTimeNow]]
    */
  def overrideSystemDateTime: Option[String] =
    configuration.getOptional[String]("override-system-date-time")

}
