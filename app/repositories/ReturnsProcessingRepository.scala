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

package repositories

import config.FrontendAppConfig
import models.returns.ProcessingEntry
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Indexes, ReplaceOptions}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ReturnsProcessingRepository @Inject() (
  mongoComponent: MongoComponent,
  appConfig: FrontendAppConfig
)(implicit ec: ExecutionContext)
    extends PlayMongoRepository[ProcessingEntry](
      collectionName = "returns-processing",
      mongoComponent = mongoComponent,
      domainFormat = ProcessingEntry.format,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("lastUpdated"),
          IndexOptions()
            .name("lastUpdatedIdx")
            .expireAfter(appConfig.cacheTtl, TimeUnit.SECONDS)
        )
      ),
      replaceIndexes = true
    ) {

  def set(entry: ProcessingEntry): Future[Unit] =
    collection
      .replaceOne(filter = Filters.equal("_id", entry.id), replacement = entry, options = ReplaceOptions().upsert(true))
      .toFuture()
      .map(_ => ())

  def get(id: String): Future[Option[ProcessingEntry]] =
    collection.find(Filters.equal("_id", id)).headOption()

}
