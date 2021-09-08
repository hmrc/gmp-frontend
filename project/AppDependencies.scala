import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    ws,
    "com.typesafe.akka" %% "akka-protobuf" % "2.6.14",
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28"  % "5.12.0",
    "uk.gov.hmrc" %% "domain"                      % "6.2.0-play-28",
    "uk.gov.hmrc" %% "http-caching-client"         % "9.5.0-play-28",
    "uk.gov.hmrc" %% "tax-year"                    % "1.1.0",
    "uk.gov.hmrc" %% "play-partials"               % "8.2.0-play-28",
    "uk.gov.hmrc" %% "emailaddress"                % "3.5.0",
    "uk.gov.hmrc" %% "govuk-template"              % "5.69.0-play-28",
    "uk.gov.hmrc" %% "play-ui"                     % "9.6.0-play-28",
    "com.typesafe.play" %% "play-json-joda"        % "2.8.1",
    "com.typesafe.play" %% "play-iteratees"        % "2.6.1",
    "com.fasterxml.jackson.module" %% "jackson-module-scala" % "2.12.3",
    compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.5" cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % "1.7.5" % Provided cross CrossVersion.full
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatest"           %% "scalatest"          % "3.2.9",
    "org.scalatestplus.play"  %% "scalatestplus-play" % "5.1.0",
    "org.scalatestplus"      %% "scalatestplus-mockito"   % "1.0.0-M2",
    "org.pegdown"             %  "pegdown"            % "1.6.0",
    "org.jsoup"               %  "jsoup"              % "1.14.2",
    "com.typesafe.play"       %% "play-test"          % PlayVersion.current,
    "org.mockito"             %  "mockito-core"       % "1.10.19",
    "com.github.tomakehurst"  %  "wiremock-jre8"      % "2.28.0",
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % "5.12.0",
    "com.vladsch.flexmark" % "flexmark-all" % "0.36.8"
  ).map(_ % "test")

  val all: Seq[ModuleID] = compile ++ test

}
