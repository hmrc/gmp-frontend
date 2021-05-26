import scoverage.ScoverageKeys._
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "gmp-frontend"


lazy val scoverageSettings: Seq[Def.Setting[_]] = {
  Seq(
    coverageExcludedPackages := "<empty>;app.*;config.*;testOnlyDoNotUseInAppConf.*;views.html.helpers*;uk.gov.hmrc.*;prod.*;forms.*",
    coverageMinimumStmtTotal := 85,
    coverageFailOnMinimum := false,
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
    scalaVersion := "2.12.13",
    libraryDependencies ++= AppDependencies.all,
    parallelExecution in Test := false,
    fork in Test := false,
    retrieveManaged := true,
    PlayKeys.playDefaultPort := 9941
  )
  .settings(
    scalacOptions ++= List(
      "-Yrangepos",
      "-Xlint:-missing-interpolator,_",
      "-Yno-adapted-args",
      "-feature",
      "-unchecked",
      "-language:implicitConversions",
      "-P:silencer:pathFilters=views;routes;TestStorage"
    )
  )
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427