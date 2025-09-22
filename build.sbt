import scoverage.ScoverageKeys._
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "gmp-frontend"

val excludedPackages: Seq[String] = Seq(
  "<empty>",
  "$anon",
  "app.events.*",
  "config.*",
  "testOnlyDoNotUseInAppConf.*",
  "views.html.helpers*",
  "uk.gov.hmrc.*",
  "prod",
  "views.html.helpers",
  "models.*",
  ".*Routes.*",
  "prod.*,forms.*"
)

lazy val scoverageSettings: Seq[Def.Setting[_]] = {
  Seq(
    coverageExcludedPackages := excludedPackages.mkString(","),
    coverageMinimumStmtTotal := 83,
    coverageFailOnMinimum := true,
    coverageHighlighting := true
  )
}

lazy val plugins: Seq[Plugins] = Seq(
  PlayScala, SbtDistributablesPlugin
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(plugins: _*)
  .settings(
    scoverageSettings,
    publishingSettings,
    scalaSettings,
    defaultSettings(),
    majorVersion := 4,

    scalaVersion := "3.3.6",

    libraryDependencies ++= AppDependencies.all,
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
    Test / parallelExecution := false,
    Test / fork := false,
    retrieveManaged := true,
    PlayKeys.playDefaultPort := 9941
)
  .settings(
      scalacOptions ++= List(
      "-feature",
      "-unchecked",
      "-language:implicitConversions",
      "-Wconf:src=routes/.*:s",
      "-Wconf:src=.*views/html.*:s",
      "-Wconf:msg=Flag.*repeatedly:s"
    ),
    scalacOptions := scalacOptions.value.distinct
  )
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427
