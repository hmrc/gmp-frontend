import play.core.PlayVersion
import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"                                %% "bootstrap-frontend-play-28" % "7.12.0",
    "uk.gov.hmrc"                                %% "domain"                     % "8.1.0-play-28",
    "uk.gov.hmrc"                                %% "http-caching-client"        % "10.0.0-play-28",
    "uk.gov.hmrc"                                %% "tax-year"                   % "3.0.0",
    "uk.gov.hmrc"                                %% "play-partials"              % "8.3.0-play-28",
    "uk.gov.hmrc"                                %% "emailaddress"               % "3.7.0",
    "uk.gov.hmrc"                                %% "play-frontend-hmrc"         % "7.19.0-play-28",
    "org.typelevel"                              %% "cats-core"                  % "2.9.0",
    "com.typesafe.play"                          %% "play-json-joda"             % "2.9.4",
    "com.fasterxml.jackson.module"               %% "jackson-module-scala"       % "2.14.2",
    compilerPlugin("com.github.ghik" %  "silencer-plugin"            % "1.7.12" cross CrossVersion.full),
    "com.github.ghik"                            %  "silencer-lib"               % "1.7.12" % Provided cross CrossVersion.full
  )

  val test: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28" % "7.12.0",
    "org.scalatest"           %% "scalatest"             % "3.2.15",
    "org.scalatestplus.play"  %% "scalatestplus-play"    % "5.1.0",
    "org.scalatestplus"       %% "scalatestplus-mockito" % "1.0.0-M2",
    "org.pegdown"             %  "pegdown"               % "1.6.0",
    "org.jsoup"               %  "jsoup"                 % "1.15.4",
    "com.typesafe.play"       %% "play-test"             % PlayVersion.current,
    "org.mockito"             %  "mockito-core"          % "5.2.0",
    "com.github.tomakehurst"  %  "wiremock-jre8"         % "2.35.0",
    "com.vladsch.flexmark"    %  "flexmark-all"          % "0.64.6"
  ).map(_ % "test")

  val all: Seq[ModuleID] = compile ++ test

}
