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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions

import play.api.mvc.{Result, Results}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.{AffinityGroup, CredentialStrength}
import uk.gov.hmrc.plasticpackagingtax.returns.config.AppConfig

trait CommonAuth {

  def appConfig: AppConfig

  protected val authData =
    credentials and name and email and externalId and internalId and affinityGroup and allEnrolments and
      agentCode and confidenceLevel and nino and saUtr and dateOfBirth and agentInformation and groupIdentifier and
      credentialRole and mdtpInformation and itmpName and itmpDateOfBirth and itmpAddress and credentialStrength and loginTimes

  protected def acceptableCredentialStrength: Predicate = {
    val strongCredentials = CredentialStrength(CredentialStrength.strong)
    // Agents are allowed to use weak credentials
    // The order of this clause is important if we wish to preserve the MFA uplift of non agents.
    // If an auth OR clause evaluates to false, the auth AlternateAuthPredicate with response with an exception
    // matching the last clause it evaluated. Strong credentials needs to be the last clause if we want to catch it
    AffinityGroup.Agent.or(strongCredentials)
  }

  protected def redirectToSignin[A]: Result =
    Results.Redirect(appConfig.loginUrl, Map("continue" -> Seq(appConfig.loginContinueUrl)))

  protected def upliftCredentialStrength[A]: Result =
    Results.Redirect(appConfig.mfaUpliftUrl,
                     Map("origin"      -> Seq(appConfig.serviceIdentifier),
                         "continueUrl" -> Seq(appConfig.loginContinueUrl)
                     )
    )

}
