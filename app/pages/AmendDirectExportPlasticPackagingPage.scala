package pages

import play.api.libs.json.JsPath

case object AmendDirectExportPlasticPackagingPage extends QuestionPage[Int] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "amendDirectExportPlasticPackaging"
}
