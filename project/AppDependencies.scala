import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {
  
  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26"     % "1.3.0",
    "uk.gov.hmrc" %% "domain"                 % "5.6.0-play-26",
    "uk.gov.hmrc" %% "http-caching-client"    % "9.0.0-play-26",
    "uk.gov.hmrc" %% "tax-year"               % "0.6.0",
    "uk.gov.hmrc" %% "play-partials"          % "6.9.0-play-26",
    "uk.gov.hmrc" %% "emailaddress"           % "3.2.0",
    "uk.gov.hmrc" %% "bulk-entity-streaming"  % "1.0.0",
    "uk.gov.hmrc" %% "auth-client"            % "2.32.0-play-26",
    "uk.gov.hmrc" %% "govuk-template"         % "5.47.0-play-26",
    "uk.gov.hmrc" %% "play-ui"                % "8.5.0-play-26",
    "com.typesafe.play" %% "play-json-joda"   % "2.7.4",
    "com.typesafe.play" %% "play-iteratees"   % "2.6.1"
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "hmrctest"           % "3.9.0-play-26",
    "org.scalatest"           %% "scalatest"          % "3.0.8",
    "org.scalatestplus.play"  %% "scalatestplus-play" % "3.1.2",
    "org.pegdown"             %  "pegdown"            % "1.6.0",
    "org.jsoup"               %  "jsoup"              % "1.11.3",
    "com.typesafe.play"       %% "play-test"          % PlayVersion.current,
    "org.mockito"             %  "mockito-core"       % "1.9.5"
  ).map(_ % "test")

  val all: Seq[ModuleID] = compile ++ test

}
