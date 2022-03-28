package pages

import play.api.libs.json.JsPath

case object AmendManufacturedPlasticPackagingPage extends QuestionPage[Int] {

  override def path: JsPath = JsPath \ toString

  override def toString: String = "amendManufacturedPlasticPackaging"
}
