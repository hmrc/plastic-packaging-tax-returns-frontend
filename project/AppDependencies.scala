import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val bootstrapVersion = "8.4.0"
  val hmrcMongoVersion = "1.7.0"

  val compile = Seq(play.sbt.PlayImport.ws,
                    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"            % bootstrapVersion,
                    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30" % "2.0.0",
                    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"    % bootstrapVersion,
                    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"            % hmrcMongoVersion
  )

  val test = Seq("org.scalatest"          %% "scalatest"                   % "3.2.16",
                 "org.scalatestplus"      %% "scalacheck-1-15"             % "3.2.10.0",
                 "org.mockito"            %% "mockito-scala-scalatest"     % "1.17.14",
                 "org.scalatestplus"      %% "mockito-4-11"                % "3.2.16.0",
                 "org.scalatestplus.play" %% "scalatestplus-play"          % "5.1.0",
                 "org.pegdown"             % "pegdown"                     % "1.6.0",
                 "org.jsoup"               % "jsoup"                       % "1.14.3",
                 "org.playframework"      %% "play-test"                   % PlayVersion.current,
                 "org.mockito"            %% "mockito-scala"               % "1.16.42",
                 "org.scalacheck"         %% "scalacheck"                  % "1.15.4",
                 "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-30"     % hmrcMongoVersion,
                 "uk.gov.hmrc"            %% "bootstrap-test-play-30"      % bootstrapVersion
  ).map(_ % "test")

  def apply(): Seq[ModuleID] = compile ++ test
}
