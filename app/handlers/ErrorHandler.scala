/*
 * Copyright 2025 HM Revenue & Customs
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

package handlers

import connectors.DownstreamServiceError
import play.api.Logging
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.InternalServerError
import play.api.mvc.{Request, RequestHeader, Result}
import play.twirl.api.Html
import uk.gov.hmrc.http.{HttpException, UpstreamErrorResponse}
import uk.gov.hmrc.play.bootstrap.frontend.http.{ApplicationException, FrontendErrorHandler}
import views.html.ErrorTemplate

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

@Singleton
class ErrorHandler @Inject() (val messagesApi: MessagesApi, view: ErrorTemplate)(implicit val ec: ExecutionContext)
    extends FrontendErrorHandler
    with I18nSupport
    with Logging {

  override def resolveError(rh: RequestHeader, ex: Throwable): Future[Result] = ex match {
    case ApplicationException(result, _) => Future.successful(result)
    case DownstreamServiceError(_, _: HttpException) | DownstreamServiceError(_, _: UpstreamErrorResponse) =>
      internalServerErrorTemplate(rh)
        .map(template => InternalServerError(template).withHeaders(CACHE_CONTROL -> "no-cache"))
    case _ =>
      logger.error("PPT_ERROR_RAISE_ALERT uncaught exception not from downstream", ex)
      internalServerErrorTemplate(rh)
        .map(template => InternalServerError(template).withHeaders(CACHE_CONTROL -> "no-cache"))
  }

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit
    rh: RequestHeader
  ): Future[Html] =
    Future.successful(view(pageTitle, heading, message))
}
