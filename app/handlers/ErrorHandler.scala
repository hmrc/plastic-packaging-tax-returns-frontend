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

package handlers

import connectors.DownstreamServiceError
import play.api.Logging
import play.api.http.HeaderNames.CACHE_CONTROL
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.Results.InternalServerError
import play.api.mvc.{Request, RequestHeader, Result}
import play.twirl.api.Html
import uk.gov.hmrc.play.bootstrap.frontend.http.{ApplicationException, FrontendErrorHandler}
import views.html.ErrorTemplate

import javax.inject.{Inject, Singleton}
import scala.language.implicitConversions

@Singleton
class ErrorHandler @Inject() (val messagesApi: MessagesApi, view: ErrorTemplate)
    extends FrontendErrorHandler with I18nSupport with Logging {

  private implicit def rhToRequest(rh: RequestHeader): Request[_] = Request(rh, "")

  override def resolveError(rh: RequestHeader, ex: Throwable): Result = ex match {
    case ApplicationException(result, _) => result
    case _: DownstreamServiceError =>
      InternalServerError(internalServerErrorTemplate(rh)).withHeaders(CACHE_CONTROL -> "no-cache")
    case _ =>
      logger.error("PPT_ERROR_RAISE_ALERT uncaught exception not from downstream", ex)
      InternalServerError(internalServerErrorTemplate(rh)).withHeaders(CACHE_CONTROL -> "no-cache")
  }

  override def standardErrorTemplate(pageTitle: String, heading: String, message: String)(implicit
    rh: Request[_]
  ): Html =
    view(pageTitle, heading, message)

}
