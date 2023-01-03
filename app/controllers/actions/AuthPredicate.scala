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

package controllers.actions

import controllers.actions.AuthenticatedIdentifierAction.IdentifierAction.{pptEnrolmentIdentifierName, pptEnrolmentKey}
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.{AffinityGroup, CredentialStrength, Enrolment}

object AuthPredicate {

  def createWithEnrolment(selectedClientIdentifier: Option[String]): Predicate =
    selectedClientIdentifier.map { clientIdentifier =>
      // If this request is decorated with a selected client identifier this indicates
      // an agent at work; we need to request the delegated authority
      Enrolment(pptEnrolmentKey).withIdentifier(pptEnrolmentIdentifierName,
                                                clientIdentifier
      ).withDelegatedAuthRule("ppt-auth")
    }.getOrElse {
      Enrolment(pptEnrolmentKey)
    }.and(acceptableCredentialStrength)

  def acceptableCredentialStrength: Predicate = {
    val strongCredentials = CredentialStrength(CredentialStrength.strong)
    // Agents are allowed to use weak credentials
    // The order of this clause is important if we wish to preserve the MFA uplift of non agents.
    // If an auth OR clause evaluates to false, the auth AlternateAuthPredicate will response with an exception
    // matching the last clause it evaluated. Strong credentials needs to be the last clause if we want to catch it
    AffinityGroup.Agent.or(strongCredentials)
  }

}
