import sbt.*

object AppDependencies {

  val bootstrapVersion = "10.7.0"
  val hmrcMongoVersion = "2.12.0"

  val compile: Seq[ModuleID] = Seq(
    play.sbt.PlayImport.ws,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"            % "13.9.0",
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30" % "3.5.0",
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"                    % hmrcMongoVersion
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"     %% "scalatest"               % "3.2.20",
    "org.scalatestplus" %% "scalacheck-1-15"         % "3.2.11.0",
    "org.scalatestplus" %% "mockito-5-23"            % "3.2.20.0",
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoVersion,
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ test
}
