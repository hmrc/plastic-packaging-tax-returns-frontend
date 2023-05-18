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

import com.google.inject.Inject
import models.requests.DataRequest
import pages.returns.credits.AvailableYears
import play.api.libs.json.JsPath
import play.api.mvc.{ActionFilter, Result}

import scala.concurrent.{ExecutionContext, Future}

class CheckCreditKeyAction @Inject()(
                                      ec: ExecutionContext
                                    ) {


  def apply(key: String): ActionFilter[DataRequest] = new ActionFilter[DataRequest] {
    def executionContext = ec

    override protected def filter[A](request: DataRequest[A]): Future[Option[Result]] = {
      if (request.userAnswers.get(AvailableYears).getOrElse(Seq.empty).exists(_.key == key)) {
        Future.successful(None)
      } else {
        throw new Exception("url hacker alert") //todo redirect somewhere?
      }
    }
  }

}
