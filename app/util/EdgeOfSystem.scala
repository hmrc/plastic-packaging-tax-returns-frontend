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

package util

import config.FrontendAppConfig

import java.time.LocalDateTime
import javax.inject.Inject

class EdgeOfSystem @Inject() (frontendAppConfig: FrontendAppConfig) {

  /** The current system date-time, or the overridden date-time if set in config
    * @return
    *  - current system date-time, if no override in-place
    *  - overridden date-time, if set 
    * @see [[FrontendAppConfig.overrideSystemDateTime]]
    */
  def localDateTimeNow: LocalDateTime = {
    frontendAppConfig
      .overrideSystemDateTime
      .getOrElse(LocalDateTime.now())
  }
  
}