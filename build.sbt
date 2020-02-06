
import scoverage.ScoverageKeys._
import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings}
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings
import uk.gov.hmrc.SbtAutoBuildPlugin

val appName = "gmp-frontend"


lazy val scoverageSettings: Seq[Def.Setting[_]] = {
  Seq(
    coverageExcludedPackages := "<empty>;app.*;config.*;testOnlyDoNotUseInAppConf.*;views.html.helpers*;uk.gov.hmrc.*;prod.*;forms.*",
    coverageMinimum := 85,
    coverageFailOnMinimum := false,
    coverageHighlighting := true
  )
}

lazy val plugins: Seq[Plugins] = Seq(
  PlayScala, SbtDistributablesPlugin, SbtAutoBuildPlugin, SbtGitVersioning, SbtArtifactory
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(plugins: _*)
  .settings(
    scoverageSettings,
    publishingSettings,
    scalaSettings,
    defaultSettings(),
    majorVersion := 4,
    scalaVersion := "2.11.12",
    libraryDependencies ++= AppDependencies.all,
    parallelExecution in Test := false,
    fork in Test := false,
    retrieveManaged := true,
    PlayKeys.playDefaultPort := 9941,
    resolvers ++= Seq(
      Resolver.bintrayRepo("hmrc", "releases"),
      Resolver.jcenterRepo,
      "hmrc-releases" at "https://artefacts.tax.service.gov.uk/artifactory/hmrc-releases/"
    )
  )