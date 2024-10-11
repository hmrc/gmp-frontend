import play.sbt.PlayImport._
import sbt._

object AppDependencies {

  private val playSuffix = "-play-30"
  private val bootstrapVersion = "9.5.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"                                %% s"bootstrap-frontend$playSuffix"  % bootstrapVersion,
    "uk.gov.hmrc"                                %% s"domain$playSuffix"              % "10.0.0",
    "uk.gov.hmrc"                                %% s"http-caching-client$playSuffix" % "12.1.0",
    "uk.gov.hmrc"                                %% s"play-partials$playSuffix"       % "10.0.0",
    "uk.gov.hmrc"                                %% s"play-frontend-hmrc$playSuffix"  % "10.12.0",
    "uk.gov.hmrc"                                %% "tax-year"                        % "5.0.0",
    "uk.gov.hmrc"                                %% "emailaddress"                    % "3.8.0",
    "org.typelevel"                              %% "cats-core"                       % "2.12.0",
    "com.fasterxml.jackson.module"               %% "jackson-module-scala"            % "2.17.2",
    compilerPlugin("com.github.ghik" %  "silencer-plugin"                 % "1.7.14" cross CrossVersion.full),
    "com.github.ghik"                            %  "silencer-lib"                    % "1.7.14" % Provided cross CrossVersion.full
  )

  val test: Seq[ModuleID] = Seq(
    "org.mockito"            %% "mockito-scala-scalatest"    % "1.17.37",
    "org.scalatestplus"      %% "scalacheck-1-17"            % "3.2.18.0",
    "org.scalatestplus.play" %% "scalatestplus-play"         % "7.0.1",
    "uk.gov.hmrc"            %% s"bootstrap-test$playSuffix" % bootstrapVersion,
    "org.jsoup"              %  "jsoup"                      % "1.18.1"
  ).map(_ % "test")

  val all: Seq[ModuleID] = compile ++ test

}
