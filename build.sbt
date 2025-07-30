import scoverage.ScoverageKeys._
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings

val appName = "gmp-frontend"


lazy val scoverageSettings: Seq[Def.Setting[_]] = {
  Seq(
    coverageExcludedPackages := "<empty>;app.*;config.*;testOnlyDoNotUseInAppConf.*;views.html.helpers*;uk.gov.hmrc.*;prod.*;forms.*",
    coverageMinimumStmtTotal := 86,
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
    scalaVersion := "2.13.16",
    libraryDependencies ++= AppDependencies.all,
    libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,
    Test / parallelExecution := false,
    Test / fork := false,
    retrieveManaged := true,
    PlayKeys.playDefaultPort := 9941
  )
  .settings(
    scalacOptions ++= List(
      "-Yrangepos",
      "-Xlint:-missing-interpolator,_",
      "-feature",
      "-unchecked",
      "-language:implicitConversions"
    )
  )
  .disablePlugins(JUnitXmlReportPlugin) //Required to prevent https://github.com/scalatest/scalatest/issues/1427