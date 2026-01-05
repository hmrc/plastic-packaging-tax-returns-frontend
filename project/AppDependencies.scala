import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val bootstrapVersion = "10.5.0"
  val hmrcMongoVersion = "2.11.0"

  val compile = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"            % "12.25.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30" % "3.4.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"                    % hmrcMongoVersion
  )

  val test = Seq(
    "org.scalatest"     %% "scalatest"               % "3.2.18",
    "org.scalatestplus" %% "scalacheck-1-15"         % "3.2.11.0",
    "org.mockito"       %% "mockito-scala-scalatest" % "1.17.31",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
