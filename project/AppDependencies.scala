import sbt._

object AppDependencies {
  import play.core.PlayVersion

  val bootstrapVersion = "7.19.0"
  val hmrcMongoVersion = "1.3.0"

  val compile = Seq(play.sbt.PlayImport.ws,
                    "uk.gov.hmrc"       %% "play-frontend-hmrc"            % "7.14.0-play-28",
                    "uk.gov.hmrc"       %% "play-conditional-form-mapping" % "1.13.0-play-28",
                    "uk.gov.hmrc"       %% "bootstrap-frontend-play-28"    % bootstrapVersion,
                    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28"            % hmrcMongoVersion
  )

  val test = Seq("org.scalatest"          %% "scalatest"                   % "3.2.16",
                 "org.scalatestplus"      %% "scalacheck-1-15"             % "3.2.10.0",
                 "org.mockito"            %% "mockito-scala-scalatest"     % "1.17.14",
                 "org.scalatestplus"      %% "mockito-4-11"                % "3.2.16.0",
                 "org.scalatestplus.play" %% "scalatestplus-play"          % "5.1.0",
                 "org.pegdown"             % "pegdown"                     % "1.6.0",
                 "org.jsoup"               % "jsoup"                       % "1.14.3",
                 "com.typesafe.play"      %% "play-test"                   % PlayVersion.current,
                 "org.mockito"            %% "mockito-scala"               % "1.16.42",
                 "org.scalacheck"         %% "scalacheck"                  % "1.15.4",
                 "uk.gov.hmrc.mongo"      %% "hmrc-mongo-test-play-28"     % hmrcMongoVersion,
                 "com.vladsch.flexmark"    % "flexmark-all"                % "0.62.2",
                 "com.github.tomakehurst"  % "wiremock-jre8"               % "2.26.3",
                 "uk.gov.hmrc"            %% "bootstrap-test-play-28"      % bootstrapVersion
  ).map(_ % "test, it")

  def apply(): Seq[ModuleID] = compile ++ test
}
