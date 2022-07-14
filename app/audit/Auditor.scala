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

package audit

import audit.returns._
import models.ExportCreditBalance
import models.financials.PPTFinancials
import models.obligations.PPTObligations
import models.returns.{ReturnDisplayApi, TaxReturn, TaxReturnObligation}
import play.api.libs.json.{JsValue, Json}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditConnector

import java.time.LocalDate
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@Singleton
class Auditor @Inject()(auditConnector: AuditConnector) {

  object Result extends Enumeration {
    type Result = Value

    val Success, Failure = Value.toString
  }

  def returnStarted(internalId: String,
                    pptReference: String,
                    headers: Seq[(String, String)])
                   (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = ReturnStarted(internalId, ReturnStarted.message, pptReference,headers)
    auditConnector.sendExplicitAudit(ReturnStarted.eventType, payload)
  }

  def amendStarted(internalId: String,
                   pptReference: String,
                   headers: Seq[(String, String)])
                  (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = AmendStarted(internalId, AmendStarted.message, pptReference, headers)
    auditConnector.sendExplicitAudit(AmendStarted.eventType, payload)
  }

  def getExportCreditsSuccess(internalId: String,
                              pptReference: String,
                              fromDate: LocalDate,
                              toDate: LocalDate,
                              response: ExportCreditBalance,
                              headers: Seq[(String, String)])
                             (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetExportCredits(internalId, pptReference, fromDate, toDate, Result.Success, Some(response), None, headers)
    auditConnector.sendExplicitAudit(GetExportCredits.eventType, payload)
  }

  def getExportCreditsFailure(internalId: String,
                              pptReference: String,
                              fromDate: LocalDate,
                              toDate: LocalDate,
                              error: String,
                              headers: Seq[(String, String)])
                             (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetExportCredits(internalId, pptReference, fromDate, toDate, Result.Failure, None, Some(error), headers)
    auditConnector.sendExplicitAudit(GetExportCredits.eventType, payload)
  }

  def getFulfilledObligationsSuccess(internalId: String,
                                     pptReference: String,
                                     response: Seq[TaxReturnObligation],
                                     headers: Seq[(String, String)])
                                    (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetFulfilledObligations(internalId, pptReference, Result.Success, Some(response), None, headers)
    auditConnector.sendExplicitAudit(GetFulfilledObligations.eventType, payload)
  }

  def getFulfilledObligationsFailure(internalId: String,
                                     pptReference: String,
                                     error: String,
                                     headers: Seq[(String, String)])
                                    (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetFulfilledObligations(internalId, pptReference, Result.Failure, None, Some(error), headers)
    auditConnector.sendExplicitAudit(GetFulfilledObligations.eventType, payload)
  }

  def getOpenObligationsSuccess(internalId: String,
                                pptReference: String,
                                response: PPTObligations,
                                headers: Seq[(String, String)])
                               (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetOpenObligations(internalId, pptReference, Result.Success, Some(response), None, headers)
    auditConnector.sendExplicitAudit(GetOpenObligations.eventType, payload)
  }

  def getOpenObligationsFailure(internalId: String,
                                pptReference: String,
                                error: String,
                                headers: Seq[(String, String)])
                               (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetOpenObligations(internalId, pptReference, Result.Failure, None, Some(error), headers)
    auditConnector.sendExplicitAudit(GetOpenObligations.eventType, payload)
  }

  def getPaymentStatementSuccess(internalId: String,
                                 pptReference: String,
                                 response: PPTFinancials,
                                 headers: Seq[(String, String)])
                                (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetPaymentStatement(internalId, pptReference, Result.Success, Some(response), None, headers)
    auditConnector.sendExplicitAudit(GetPaymentStatement.eventType, payload)
  }

  def getPaymentStatementFailure(internalId: String,
                                 pptReference: String,
                                 error: String,
                                 headers: Seq[(String, String)])
                                (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetPaymentStatement(internalId, pptReference, Result.Failure, None, Some(error), headers)
    auditConnector.sendExplicitAudit(GetPaymentStatement.eventType, payload)
  }

  def submitAmendSuccess(internalId: String,
                         pptReference: String,
                         taxReturn: TaxReturn,
                         response: JsValue,
                         headers: Seq[(String, String)])
                        (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = SubmitAmend(internalId, pptReference, Result.Success, taxReturn, Some(response), None, headers)
    auditConnector.sendExplicitAudit(SubmitAmend.eventType, payload)
  }

  def submitAmendFailure(internalId: String,
                         pptReference: String,
                         taxReturn: TaxReturn,
                         error: String,
                         headers: Seq[(String, String)])
                        (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = SubmitAmend(internalId, pptReference, Result.Failure, taxReturn, None, Some(error), headers)
    auditConnector.sendExplicitAudit(SubmitAmend.eventType, payload)
  }

  def submitReturnSuccess(internalId: String,
                          pptReference: String,
                          taxReturn: TaxReturn,
                          response: JsValue,
                          headers: Seq[(String, String)])
                         (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = SubmitReturn(internalId, pptReference, Result.Success, taxReturn, Some(response), None, headers)
    auditConnector.sendExplicitAudit(SubmitReturn.eventType, payload)
  }

  def submitReturnFailure(internalId: String,
                          pptReference: String,
                          taxReturn: TaxReturn,
                          error: String,
                          headers: Seq[(String, String)])
                         (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = SubmitReturn(internalId, pptReference, Result.Failure, taxReturn, None, Some(error), headers)
    auditConnector.sendExplicitAudit(SubmitReturn.eventType, payload)
  }

  def getReturnSuccess(internalId: String,
                       periodKey: String,
                       response: ReturnDisplayApi,
                       headers: Seq[(String, String)])
                      (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetReturn(internalId, periodKey, Result.Success, Some(response), None, headers)
    auditConnector.sendExplicitAudit(GetReturn.eventType, payload)
  }

  def getReturnFailure(internalId: String,
                       periodKey: String,
                       error: String,
                       headers: Seq[(String, String)])
                      (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetReturn(internalId, periodKey, Result.Failure, None, Some(error), headers)
    auditConnector.sendExplicitAudit(GetReturn.eventType, payload)
  }
}

