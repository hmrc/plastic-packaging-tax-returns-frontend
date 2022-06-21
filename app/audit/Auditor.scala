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
    val payload = GetExportCreditsSuccess(internalId, pptReference, fromDate, toDate, response, headers)
    auditConnector.sendExplicitAudit(GetExportCreditsSuccess.eventType, payload)
  }

  def getExportCreditsFailure(internalId: String,
                              pptReference: String,
                              fromDate: LocalDate,
                              toDate: LocalDate,
                              error: String,
                              headers: Seq[(String, String)])
                             (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetExportCreditsFailure(internalId, pptReference, fromDate, toDate, error, headers)
    auditConnector.sendExplicitAudit(GetExportCreditsFailure.eventType, payload)
  }

  def getFulfilledObligationsSuccess(internalId: String,
                                     pptReference: String,
                                     response: Seq[TaxReturnObligation],
                                     headers: Seq[(String, String)])
                                    (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetFulfilledObligationsSuccess(internalId, pptReference, response, headers)
    auditConnector.sendExplicitAudit(GetFulfilledObligationsSuccess.eventType, payload)
  }

  def getFulfilledObligationsFailure(internalId: String,
                                     pptReference: String,
                                     error: String,
                                     headers: Seq[(String, String)])
                                    (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetFulfilledObligationsFailure(internalId, pptReference, error, headers)
    auditConnector.sendExplicitAudit(GetFulfilledObligationsFailure.eventType, payload)
  }

  def getOpenObligationsSuccess(internalId: String,
                                pptReference: String,
                                response: PPTObligations,
                                headers: Seq[(String, String)])
                               (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetOpenObligationsSuccess(internalId, pptReference, response, headers)
    auditConnector.sendExplicitAudit(GetOpenObligationsSuccess.eventType, payload)
  }

  def getOpenObligationsFailure(internalId: String,
                                pptReference: String,
                                error: String,
                                headers: Seq[(String, String)])
                               (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetOpenObligationsFailure(internalId, pptReference, error, headers)
    auditConnector.sendExplicitAudit(GetOpenObligationsFailure.eventType, payload)
  }

  def getPaymentStatementFailure(internalId: String,
                                 pptReference: String,
                                 error: String,
                                 headers: Seq[(String, String)])
                                (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetPaymentStatementFailure(internalId, pptReference, error, headers)
    auditConnector.sendExplicitAudit(GetPaymentStatementFailure.eventType, payload)
  }

  def getPaymentStatementSuccess(internalId: String,
                                 pptReference: String,
                                 response: PPTFinancials,
                                 headers: Seq[(String, String)])
                                (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetPaymentStatementSuccess(internalId, pptReference, response, headers)
    auditConnector.sendExplicitAudit(GetPaymentStatementSuccess.eventType, payload)
  }

  def submitAmendFailure(internalId: String,
                         pptReference: String,
                         taxReturn: TaxReturn,
                         error: String,
                         headers: Seq[(String, String)])
                        (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = SubmitAmendFailure(internalId, pptReference, taxReturn, error, headers)
    auditConnector.sendExplicitAudit(SubmitAmendFailure.eventType, payload)
  }

  def submitAmendSuccess(internalId: String,
                         pptReference: String,
                         taxReturn: TaxReturn,
                         response: JsValue,
                         headers: Seq[(String, String)])
                        (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = SubmitAmendSuccess(internalId, pptReference, taxReturn, response, headers)
    auditConnector.sendExplicitAudit(SubmitAmendSuccess.eventType, payload)
  }

  def submitReturnFailure(internalId: String,
                          pptReference: String,
                          taxReturn: TaxReturn,
                          error: String,
                          headers: Seq[(String, String)])
                         (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = SubmitReturnFailure(internalId, pptReference, taxReturn, error, headers)
    auditConnector.sendExplicitAudit(SubmitReturnFailure.eventType, payload)
  }

  def submitReturnSuccess(internalId: String,
                          pptReference: String,
                          taxReturn: TaxReturn,
                          response: JsValue,
                          headers: Seq[(String, String)])
                         (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = SubmitReturnSuccess(internalId, pptReference, taxReturn, response, headers)
    auditConnector.sendExplicitAudit(SubmitReturnSuccess.eventType, payload)
  }

  def getReturnSuccess(internalId: String,
                       periodKey: String,
                       response: ReturnDisplayApi,
                       headers: Seq[(String, String)])
                      (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetReturnSuccess(internalId, periodKey, response, headers)
    auditConnector.sendExplicitAudit(GetReturnSuccess.eventType, payload)
  }

  def getReturnFailure(internalId: String,
                       periodKey: String,
                       error: String,
                       headers: Seq[(String, String)])
                      (implicit hc: HeaderCarrier, ex: ExecutionContext): Unit = {
    val payload = GetReturnFailure(internalId, periodKey, error, headers)
    auditConnector.sendExplicitAudit(GetReturnFailure.eventType, payload)
  }
}

