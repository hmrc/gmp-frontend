import play.sbt.PlayImport.*
import sbt.*

object AppDependencies {

  private val playVersion = "play-30"
  private val bootstrapVersion = "10.1.0"
  private val hmrcMongoVersion = "2.7.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"                                %% s"bootstrap-frontend-$playVersion"  % bootstrapVersion,
    "uk.gov.hmrc"                                %% s"domain-$playVersion"              % "13.0.0",
    "uk.gov.hmrc"                                %% s"http-caching-client-$playVersion" % "12.1.0",
    "uk.gov.hmrc"                                %% s"play-partials-$playVersion"       % "10.0.0",
    "uk.gov.hmrc"                                %% s"play-frontend-hmrc-$playVersion"  % "12.8.0",
    "uk.gov.hmrc"                                %% "tax-year"                          % "6.0.0",
    "org.typelevel"                              %% "cats-core"                         % "2.12.0",
    "com.fasterxml.jackson.module"               %% "jackson-module-scala"              % "2.18.2",
    "uk.gov.hmrc.mongo"                          %% s"hmrc-mongo-$playVersion"          % hmrcMongoVersion
  )

  val test: Seq[ModuleID] = Seq(
    "org.scalatestplus"      %% "mockito-4-11"                  % "3.2.17.0",
    "org.scalatestplus"      %% "scalacheck-1-17"               % "3.2.18.0",
    "org.scalatestplus.play" %% "scalatestplus-play"            % "7.0.1",
    "uk.gov.hmrc"            %% s"bootstrap-test-$playVersion"  % bootstrapVersion,
    "org.jsoup"              %  "jsoup"                         % "1.18.3",
    "uk.gov.hmrc"            %% s"domain-test-$playVersion"     % "13.0.0",
    "uk.gov.hmrc.mongo"      %% s"hmrc-mongo-test-$playVersion" % hmrcMongoVersion
  ).map(_ % "test")

  val all: Seq[ModuleID] = compile ++ test

}
