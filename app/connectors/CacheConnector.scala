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

package connectors

import config.FrontendAppConfig
import models.UserAnswers
import play.api.libs.json.Writes
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CacheConnector @Inject()(config: FrontendAppConfig,
                               implicit val httpClient: HttpClient)
                              (implicit ec: ExecutionContext) {


  def get(pptReference: String)(implicit hc: HeaderCarrier): Future[Option[UserAnswers]] = {

    httpClient.GET[Option[UserAnswers]](config.pptCacheGetUrl(pptReference))

  }

  def set(pptReference: String, userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[HttpResponse] = {

    httpClient.POST(config.pptCacheSetUrl(pptReference), userAnswers)(
      implicitly[Writes[UserAnswers]],
      implicitly[HttpReads[HttpResponse]],
      hc.withExtraHeaders("Csrf-Token" -> "nocheck"),
      implicitly
    )

  }
}
