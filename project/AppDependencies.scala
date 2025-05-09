import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  private val playVersion = "play-30"
  private val bootstrapVersion = "9.11.0"
  private val hmrcMongoVersion = "2.4.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"                                %% s"bootstrap-frontend-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc"                                %% s"domain-$playVersion"              % "10.0.0",
    "uk.gov.hmrc"                                %% s"http-caching-client-$playVersion" % "12.1.0",
    "uk.gov.hmrc"                                %% s"play-partials-$playVersion"       % "10.0.0",
    "uk.gov.hmrc"                                %% s"play-frontend-hmrc-$playVersion"  % "11.10.0",
    "uk.gov.hmrc"                                %% "tax-year"                        % "5.0.0",
    "org.typelevel"                              %% "cats-core"                       % "2.12.0",
    "com.fasterxml.jackson.module"               %% "jackson-module-scala"            % "2.18.2",
    compilerPlugin("com.github.ghik" %  "silencer-plugin"                 % "1.7.14" cross CrossVersion.full),
    "com.github.ghik"                            %  "silencer-lib"                    % "1.7.14" % Provided cross CrossVersion.full,
    "uk.gov.hmrc.mongo"                          %% s"hmrc-mongo-$playVersion"          % hmrcMongoVersion

  )

  val test: Seq[ModuleID] = Seq(
    "org.mockito"            %% "mockito-scala-scalatest"    % "1.17.37",
    "org.scalatestplus"      %% "scalacheck-1-17"            % "3.2.18.0",
    "org.scalatestplus.play" %% "scalatestplus-play"         % "7.0.1",
    "uk.gov.hmrc"            %% s"bootstrap-test-$playVersion" % bootstrapVersion,
    "org.jsoup"              %  "jsoup"                      % "1.18.3",
    "uk.gov.hmrc.mongo"      %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion
  ).map(_ % "test")

  val all: Seq[ModuleID] = compile ++ test

}
