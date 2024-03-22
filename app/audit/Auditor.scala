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

package audit

import audit.returns._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class Auditor @Inject() (auditConnector: AuditConnector) {

  def returnStarted(internalId: String, pptReference: String)(implicit
    hc: HeaderCarrier,
    ex: ExecutionContext
  ): Unit = {
    val payload = ReturnStarted(internalId, ReturnStarted.message, pptReference)
    auditConnector.sendExplicitAudit(ReturnStarted.eventType, payload)
  }

  def amendStarted(internalId: String, pptReference: String)(implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = AmendStarted(internalId, AmendStarted.message, pptReference)
    auditConnector.sendExplicitAudit(AmendStarted.eventType, payload)
  }

}
