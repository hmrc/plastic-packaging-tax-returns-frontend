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

package models

import cacheables.ReturnObligationCacheable
import models.requests.DataRequest
import models.returns.TaxReturnObligation
import play.api.mvc.Result
import play.api.mvc.Results.Redirect

import scala.concurrent.Future

object ReturnsUserAnswers {

  /** Fetches the obligation for the current journey, or redirects to the account page 
    * @param request the current DataRequest (with current user answers)
    * @param block a function to call with the obligation
    * @return whatever block returns if obligation found, otherwise a redirect to the account page 
    */
  def checkObligation(request: DataRequest[_])(block: TaxReturnObligation => Future[Result]) =
    request.userAnswers.get(ReturnObligationCacheable) match {
      case Some(obligation) => block(obligation)
      case None => Future.successful(Redirect(controllers.routes.IndexController.onPageLoad))
    }


  /** Future free version of [[models.ReturnsUserAnswers#checkObligation]]
    */
  def checkObligationSync(request: DataRequest[_])(block: TaxReturnObligation => Result) =
    request.userAnswers.get(ReturnObligationCacheable) match {
      case Some(obligation) => block(obligation)
      case None => Redirect(controllers.routes.IndexController.onPageLoad)
    }

}
