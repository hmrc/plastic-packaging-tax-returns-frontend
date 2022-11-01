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

package controllers.changeGroupLead

import config.FrontendAppConfig
import connectors.{CacheConnector, SubscriptionConnector}
import controllers.actions._
import forms.changeGroupLead.SelectNewGroupLeadForm
import models.requests.DataRequest
import pages.ChooseNewGroupLeadPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.changeGroupLead.ChooseNewGroupLeadView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}


//todo move
class FeatureGuard @Inject() (frontendAppConfig: FrontendAppConfig) {
  def check(): Unit = {
    if (!frontendAppConfig.isFeatureEnabledChangeOfGroupLead)
      throw new RuntimeException("Change of group lead feature is not enabled")
  }
}

class ChooseNewGroupLeadController @Inject() (
  override val messagesApi: MessagesApi,
  journeyAction: JourneyAction,
  val controllerComponents: MessagesControllerComponents,
  view: ChooseNewGroupLeadView,
  form: SelectNewGroupLeadForm,
  cacheConnector: CacheConnector,
  featureGuard: FeatureGuard,
  subscriptionConnector: SubscriptionConnector
)
  (implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  def onPageLoad(): Action[AnyContent] = journeyAction.async {
    implicit request =>
      featureGuard.check()
      getMembers{ members =>
        val preparedForm = request.userAnswers.fill(ChooseNewGroupLeadPage, form(members))

        Future.successful(Ok(view(preparedForm, members)))
      }
  }

  def onSubmit(): Action[AnyContent] = journeyAction.async {
    implicit request =>
      featureGuard.check()
      getMembers{ members =>
        form(members)
          .bindFromRequest()
          .fold(
            errorForm => Future.successful(BadRequest(view(errorForm, members))),
            selectedMember =>
              request.userAnswers
                .setOrFail(ChooseNewGroupLeadPage, selectedMember)
                .save(cacheConnector.saveUserAnswerFunc(request.pptReference))
                .map(_ => Redirect(controllers.routes.IndexController.onPageLoad))
          )
      }
  }

  private def getMembers(block: Seq[String] => Future[Result])(implicit request: DataRequest[AnyContent]): Future[Result] =
    subscriptionConnector
      .get(request.pptReference)
      .flatMap {
        case Left(error) => throw new RuntimeException("failed") //todo
        case Right(subscription) =>
          val members = Seq("a", "c", "b")
//            subscription.groupPartnershipSubscription.get
//            .groupPartnershipDetails.map(
//            details => details.organisationDetails.get
//              .organisationName
//          )
          block(members)
      }

}
