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

package uk.gov.hmrc.plasticpackagingtax.returns.controllers.subscriptions

import javax.inject.{Inject, Singleton}
import play.api.data.Form
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.plasticpackagingtax.returns.connectors.SubscriptionConnector
import uk.gov.hmrc.plasticpackagingtax.returns.controllers.actions.AuthAction
import uk.gov.hmrc.plasticpackagingtax.returns.forms.subscriptions.Name
import uk.gov.hmrc.plasticpackagingtax.returns.models.request.{JourneyAction, JourneyRequest}
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.subscriptionDisplay.ChangeOfCircumstanceDetails
import uk.gov.hmrc.plasticpackagingtax.returns.models.subscription.subscriptionUpdate.SubscriptionUpdateRequest
import uk.gov.hmrc.plasticpackagingtax.returns.views.html.subscriptions.primary_contact_name_page
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class PrimaryContactNameController @Inject() (
  authenticate: AuthAction,
  journeyAction: JourneyAction,
  subscriptionConnector: SubscriptionConnector,
  mcc: MessagesControllerComponents,
  page: primary_contact_name_page
)(implicit ec: ExecutionContext)
    extends FrontendController(mcc) with I18nSupport {

  def displayPage(): Action[AnyContent] =
    (authenticate andThen journeyAction).async { implicit request =>
      subscriptionConnector.get(request.pptReference)
        .map { subscription =>
          Ok(page(Name.form().fill(Name(subscription.primaryContactDetails.name))))
        }
    }

  def submit(): Action[AnyContent] =
    (authenticate andThen journeyAction).async { implicit request =>
      Name.form()
        .bindFromRequest()
        .fold((formWithErrors: Form[Name]) => Future.successful(BadRequest(page(formWithErrors))),
              name =>
                updateSubscription(request.pptReference, name).flatMap { updateSubscription =>
                  subscriptionConnector.update(request.pptReference, updateSubscription).map { _ =>
                    Redirect(routes.ViewSubscriptionController.displayPage())
                  }
                }
        )
    }

  private def updateSubscription(pptReference: String, formData: Name)(implicit
    req: JourneyRequest[_]
  ): Future[SubscriptionUpdateRequest] =
    subscriptionConnector.get(pptReference).map { subscription =>
      val updatedPrimaryContactDetails =
        subscription.primaryContactDetails.copy(name = formData.value)
      val updatedLegalEntityDetails =
        subscription.legalEntityDetails.copy(regWithoutIDFlag = Some(false))

      val updatedSubscription =
        subscription.copy(legalEntityDetails = updatedLegalEntityDetails,
                          primaryContactDetails = updatedPrimaryContactDetails,
                          changeOfCircumstanceDetails =
                            Some(ChangeOfCircumstanceDetails("Update to details"))
        )
      SubscriptionUpdateRequest(updatedSubscription)
    }

}
