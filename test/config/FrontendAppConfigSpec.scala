/*
 * Copyright 2023 HM Revenue & Customs
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

import org.mockito.ArgumentMatchersSugar.any
import org.mockito.MockitoSugar
import org.mockito.integrations.scalatest.ResetMocksAfterEachTest
import org.scalatest.BeforeAndAfterEach
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatestplus.play.PlaySpec
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

import java.time.LocalDateTime

class FrontendAppConfigSpec extends PlaySpec
  with MockitoSugar
  with BeforeAndAfterEach
  with ResetMocksAfterEachTest {

  private val configuration = mock[Configuration]
  private val servicesConfig = mock[ServicesConfig]

  val frontendAppConfig = new FrontendAppConfig(configuration, servicesConfig)

  "overrideSystemDateTime" should {

    "parse the override if it's there" in {
      when(configuration.getOptional[String](any)(any)) thenReturn Some("2023-03-31T23:59:58")
      frontendAppConfig.overrideSystemDateTime.value mustBe LocalDateTime.of(2023, 3, 31, 23, 59, 58)
    }

    "return None if override not present" in {
      when(configuration.getOptional[String](any)(any)) thenReturn None
      frontendAppConfig.overrideSystemDateTime mustBe empty
    }

  }

  private def validAppConfig(overrideSystemDateTime: String): Config =
    ConfigFactory.parseString(s"""
    |host="http://localhost:8505"
    |appName="plastic-packaging-tax-returns-frontend"
    |contact-frontend.host="http://localhost:9250"
    |urls {
    |  login = "http://localhost:9949/auth-login-stub/gg-sign-in"
    |  signOut = "http://localhost:9553/bas-gateway/sign-out-without-state"
    |  signedOut = "http://localhost:8505/plastic-packaging-tax/account/signed-out"
    |  loginContinue = "http://localhost:8505/plastic-packaging-tax/account"
    |  mfaUplift = "http://localhost:9553/bas-gateway/uplift-mfa"
    |  govUk = "https://www.gov.uk"
    |  pptGuidanceLink = "https://www.gov.uk/guidance/check-if-you-need-to-register-for-plastic-packaging-tax"
    |  pptCompleteReturnGuidanceLink = "https://www.gov.uk/guidance/completing-your-plastic-packaging-tax-return"
    |  pptLiablePackagingGuidanceLink = "https://www.gov.uk/guidance/work-out-which-packaging-is-subject-to-plastic-packaging-tax"
    |  pptExcludedPackagingGuidanceLink = "https://www.gov.uk/guidance/check-which-plastic-packaging-is-exempt-from-plastic-packaging-tax#packaging-excluded-from-the-tax"
    |  pptCreditsGuidanceLink = "https://www.gov.uk/guidance/claim-a-credit-or-defer-paying-plastic-packaging-tax#components-youve-already-paid-tax-on-which-are-exported-or-converted"
    |  claimingCreditGuidanceUrl = "https://www.gov.uk/guidance/claim-a-credit-or-defer-paying-plastic-packaging-tax#components-youve-already-paid-tax-on-which-are-exported-or-converted"
    |  businessAccount= "http://localhost:9020/business-account"
    |  exitSurvey = "http://localhost:9514/feedback/plastic-packaging-tax-returns"
    |  userResearchUrl = "https://signup.take-part-in-research.service.gov.uk/?utm_campaign=List_Plastic_Packaging_Tax&utm_source=&utm_medium=other&t=HMRC&id=256"
    |  pptRegistrationsInfoLink = "https://www.gov.uk/government/publications/introduction-of-plastic-packaging-tax/plastic-packaging-tax"
    |  pptRecycledPlasticGuidanceLink = "https://www.gov.uk/guidance/work-out-which-packaging-is-subject-to-plastic-packaging-tax#recycled-plastic"
    |  addMemberToGroup = "http://localhost:8503/register-for-plastic-packaging-tax/list-group-members"
    |}
    |features {
    |    returnsEnabled = true
    |    creditsForReturnsEnabled = true
    |    paymentsEnabled = true
    |    deRegistrationEnabled = true
    |    amendsEnabled = true
    |    changeOfGroupLead = true
    |    override-system-date-time = "${overrideSystemDateTime}"
    |    welsh-translation: true
    |}
    |timeout-dialog {
    |  timeout=900
    |  countdown=120
    |}
    |mongodb {
    |  uri="mongodb://localhost:27017/plastic-packaging-tax-returns-frontend"
    |  timeToLiveInSeconds=900
    |}
    """.stripMargin)

  private val validServicesConfiguration = Configuration(validAppConfig("2023-03-31T23:59:59"))

  private def servicesConfig(conf: Configuration) = new ServicesConfig(conf)
  private def appConfig(conf: Configuration) = new FrontendAppConfig(conf, servicesConfig(conf))

  "configuration" should {

    "return override-system-date-time" in {
      val configService: FrontendAppConfig = appConfig(validServicesConfiguration)

      configService.overrideSystemDateTime mustBe
        Some(LocalDateTime.of(2023, 3, 31, 23, 59, 59))
    }

    "return a none for invalid override-system-date-time" in {
      val configService: FrontendAppConfig = appConfig(Configuration(validAppConfig("false")))

      configService.overrideSystemDateTime mustBe None
    }
  }

}
