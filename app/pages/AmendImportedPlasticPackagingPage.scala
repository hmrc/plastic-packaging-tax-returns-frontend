package pages

import play.api.libs.json.JsPath

case object AmendImportedPlasticPackagingPage extends QuestionPage[Int] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "amendImportedPlasticPackaging"
}
