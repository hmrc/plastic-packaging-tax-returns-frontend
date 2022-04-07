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

/*
 * Copyright 2021 HM Revenue & Customs
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

package uk.gov.hmrc.plasticpackagingtax.returns.config

import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import uk.gov.hmrc.hmrcfrontend.views.viewmodels.language.{Cy, En, Language}
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.{routes => returnsRoutes}

@Singleton
class AppConfig @Inject() (config: Configuration, val servicesConfig: ServicesConfig) {

  lazy val welshEnabled: Boolean = config.get[Boolean]("lang.welsh.enabled")

  def languageLinks: Seq[(Language, String)] =
    Seq((En, returnsRoutes.LanguageController.enGb().url),
        (Cy, returnsRoutes.LanguageController.cyGb().url)
    )

  lazy val assetsUrl: String = config.get[String]("assets.url")

  lazy val assetsPrefix: String = assetsUrl + config.get[String]("assets.version")

  lazy val serviceIdentifier = "plastic-packaging-tax"

  lazy val selfBaseUrl: String = config
    .getOptional[String]("platform.frontend.host")
    .getOrElse("http://localhost:9250")

  lazy val loginUrl         = config.get[String]("urls.login")
  lazy val loginContinueUrl = config.get[String]("urls.loginContinue")
  lazy val mfaUpliftUrl     = config.get[String]("urls.mfaUplift")

  lazy val pptServiceHost: String =
    servicesConfig.baseUrl("plastic-packaging-tax-returns")

  lazy val pptReturnsUrl: String           = s"$pptServiceHost/returns"
  lazy val pptReturnsSubmissionUrl: String = s"$pptServiceHost/returns-submission"

  def pptReturnUrl(pptReference: String): String = s"$pptReturnsUrl/$pptReference"

  def pptReturnSubmissionUrl(pptReference: String): String =
    s"$pptReturnsSubmissionUrl/$pptReference"

  def pptSubscriptionUrl(pptReference: String): String =
    s"$pptServiceHost/subscriptions/$pptReference"

  def pptObligationUrl(pptReference: String): String =
    s"$pptServiceHost/obligations/open/$pptReference"

  def pptFinancialsUrl(pptReference: String): String =
    s"$pptServiceHost/financials/open/$pptReference"

  def pptExportCreditsUrl(pptReference: String, fromDate: LocalDate, toDate: LocalDate): String =
    s"$pptServiceHost/export-credits/$pptReference?fromDate=$fromDate&toDate=$toDate"

  lazy val pptGuidanceUrl: String = config.get[String]("urls.pptGuidanceLink")

  lazy val pptCompleteReturnGuidanceUrl: String =
    config.get[String]("urls.pptCompleteReturnGuidanceLink")

  lazy val pptLiablePackagingGuidanceLink =
    config.get[String]("urls.pptLiablePackagingGuidanceLink")

  lazy val pptExcludedPackagingGuidanceLink =
    config.get[String]("urls.pptExcludedPackagingGuidanceLink")

  lazy val feedbackAuthenticatedLink: String = config.get[String]("urls.feedback.authenticatedLink")

  lazy val exitSurveyUrl = config.get[String]("urls.exitSurvey")
  lazy val govUkUrl      = config.get[String]("urls.govUk")

  lazy val feedbackUnauthenticatedLink: String =
    config.get[String]("urls.feedback.unauthenticatedLink")

  def authenticatedFeedbackUrl(): String =
    s"$feedbackAuthenticatedLink?service=${serviceIdentifier}"

  def unauthenticatedFeedbackUrl(): String =
    s"$feedbackUnauthenticatedLink?service=${serviceIdentifier}"

  lazy val pptRegistrationFrontEnd =
    config.getOptional[String]("platform.frontend.host").getOrElse(
      servicesConfig.baseUrl("ppt-registration-frontend")
    )

  lazy val pptRegistrationUrl =
    s"$pptRegistrationFrontEnd/register-for-plastic-packaging-tax/start"

  lazy val pptRegistrationAmendUrl =
    s"$pptRegistrationFrontEnd/register-for-plastic-packaging-tax/amend-registration"

  lazy val pptRegistrationManageGroupUrl =
    s"$pptRegistrationFrontEnd/register-for-plastic-packaging-tax/manage-group-members"

  lazy val pptRegistrationManagePartnersUrl =
    s"$pptRegistrationFrontEnd/register-for-plastic-packaging-tax/manage-partners"

  lazy val pptRegistrationDeregisterUrl =
    s"$pptRegistrationFrontEnd/register-for-plastic-packaging-tax/deregister"

  lazy val businessAccountUrl: String = config.get[String]("urls.businessAccount")

}
