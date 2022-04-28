/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (
  configuration: Configuration,
  val servicesConfig: ServicesConfig
) {

  val host: String           = configuration.get[String]("host")
  val appName: String        = configuration.get[String]("appName")
  lazy val mfaUpliftUrl      = configuration.get[String]("urls.mfaUplift")
  lazy val serviceIdentifier = "plastic-packaging-tax"

  private val contactHost                  = configuration.get[String]("contact-frontend.host")
  private val contactFormServiceIdentifier = "plastic-packaging-tax-returns-frontend-scaffold"

  def feedbackUrl(implicit request: RequestHeader): String =
    s"$contactHost/contact/beta-feedback?service=$contactFormServiceIdentifier&backUrl=${SafeRedirectUrl(host + request.uri).encodedUrl}"

  val loginUrl: String         = configuration.get[String]("urls.login")
  val loginContinueUrl: String = configuration.get[String]("urls.loginContinue")
  val signOutUrl: String       = configuration.get[String]("urls.signOut")

  val exitSurveyUrl: String = configuration.get[String]("urls.exitSurvey")
  val signedOutUrl: String  = configuration.get[String]("urls.signedOut")

  val languageTranslationEnabled: Boolean =
    configuration.get[Boolean]("features.welsh-translation")

  def languageMap: Map[String, Lang] = Map("en" -> Lang("en"), "cy" -> Lang("cy"))

  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Int = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  lazy val pptServiceHost: String =
    servicesConfig.baseUrl("plastic-packaging-tax-returns")

  private lazy val pptReturnsSubmissionUrl: String = s"$pptServiceHost/returns-submission"
  private lazy val pptReturnsAmendUrl: String      = s"$pptServiceHost/returns-amend"

  lazy val pptRegistrationFrontEnd =
    configuration.getOptional[String]("platform.frontend.host").getOrElse(
      servicesConfig.baseUrl("ppt-registration-frontend")
    )

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

  def isDeRegistrationFeatureEnabled: Boolean =
    isFeatureEnabled(Features.deRegistrationEnabled)

  def isFeatureEnabled(name: String): Boolean =
    configuration.getOptional[Boolean](s"features.$name").getOrElse(false)

  def pptSubscriptionUrl(pptReference: String): String =
    s"$pptServiceHost/subscriptions/$pptReference"

  lazy val pptCompleteReturnGuidanceUrl: String =
    configuration.get[String]("urls.pptCompleteReturnGuidanceLink")

  def pptOpenObligationUrl(pptReference: String): String =
    s"$pptServiceHost/obligations/open/$pptReference"

  def pptFulfilledObligationUrl(pptReference: String): String =
    s"$pptServiceHost/obligations/fulfilled/$pptReference"

  def pptFinancialsUrl(pptReference: String): String =
    s"$pptServiceHost/financials/open/$pptReference"

  def makePaymentUrl: String = servicesConfig.baseUrl("pay-api") + "/pay-api/plastic-packaging-tax/journey/start"

  def pptCacheGetUrl(pptReference: String): String =
    s"$pptServiceHost/cache/get/$pptReference"

  def pptCacheSetUrl(pptReference: String): String =
    s"$pptServiceHost/cache/set/$pptReference"

}
