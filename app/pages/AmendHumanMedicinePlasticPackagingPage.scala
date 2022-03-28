package pages

import play.api.libs.json.JsPath

case object AmendHumanMedicinePlasticPackagingPage extends QuestionPage[Int] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "amendHumanMedicinePlasticPackaging"
}
