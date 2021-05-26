import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-26"  % "5.3.0",
    "uk.gov.hmrc" %% "domain"                      % "5.11.0-play-26",
    "uk.gov.hmrc" %% "http-caching-client"         % "9.5.0-play-26",
    "uk.gov.hmrc" %% "tax-year"                    % "1.1.0",
    "uk.gov.hmrc" %% "play-partials"               % "8.1.0-play-26",
    "uk.gov.hmrc" %% "emailaddress"                % "3.5.0",
    "uk.gov.hmrc" %% "govuk-template"              % "5.66.0-play-26",
    "uk.gov.hmrc" %% "play-ui"                     % "9.4.0-play-26",
    "com.typesafe.play" %% "play-json-joda"        % "2.7.4",
    "com.typesafe.play" %% "play-iteratees"        % "2.6.1",
    compilerPlugin("com.github.ghik" % "silencer-plugin" % "1.7.4" cross CrossVersion.full),
    "com.github.ghik" % "silencer-lib" % "1.7.4" % Provided cross CrossVersion.full
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "hmrctest"           % "3.10.0-play-26",
    "org.scalatest"           %% "scalatest"          % "3.0.9",
    "org.scalatestplus.play"  %% "scalatestplus-play" % "3.1.3",
    "org.pegdown"             %  "pegdown"            % "1.6.0",
    "org.jsoup"               %  "jsoup"              % "1.13.1",
    "com.typesafe.play"       %% "play-test"          % PlayVersion.current,
    "org.mockito"             %  "mockito-core"       % "1.10.19",
    "com.github.tomakehurst"  %  "wiremock-jre8"      % "2.28.0",
    "uk.gov.hmrc" %% "bootstrap-play-26" % "4.0.0"
  ).map(_ % "test")

  val all: Seq[ModuleID] = compile ++ test

}
