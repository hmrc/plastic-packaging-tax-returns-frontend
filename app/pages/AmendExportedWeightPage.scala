package pages

import play.api.libs.json.JsPath

case object AmendExportedWeightPage extends QuestionPage[Long] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "amendExportedWeight"
}
