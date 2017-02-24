import play.routes.compiler.StaticRoutesGenerator
import sbt.Keys._
import sbt.Tests.{Group, SubProcess}
import sbt._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import scoverage.ScoverageSbtPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning
import play.sbt.routes.RoutesKeys.routesGenerator

trait MicroService {

  import uk.gov.hmrc._
  import DefaultBuildSettings._
  import uk.gov.hmrc.SbtAutoBuildPlugin

  import TestPhases._

  val appName: String

  lazy val appDependencies : Seq[ModuleID] = Seq.empty
  lazy val plugins : Seq[Plugins] = Seq(play.sbt.PlayScala)
  lazy val playSettings : Seq[Setting[_]] = Seq.empty

  lazy val scoverageSettings = {
    import ScoverageSbtPlugin._

    Seq(
      ScoverageKeys.coverageExcludedPackages := "<empty>;app.*;config.*;testOnlyDoNotUseInAppConf.*;views.*;uk.gov.hmrc.*;prod.*;forms.*",
      ScoverageKeys.coverageMinimum := 90,
      ScoverageKeys.coverageFailOnMinimum := true,
      ScoverageKeys.coverageHighlighting := true
    )
  }

  lazy val microservice = Project(appName, file("."))
    .enablePlugins(plugins : _*)
    .settings(playSettings : _*)
    .settings(scoverageSettings : _*)
    .settings(publishingSettings: _*)
    .settings(scalaSettings: _*)
    .settings(defaultSettings(): _*)
    .settings(
      targetJvm := "jvm-1.8",
      libraryDependencies ++= appDependencies,
      parallelExecution in Test := false,
      fork in Test := false,
      retrieveManaged := true,
      routesGenerator := StaticRoutesGenerator
    )
    .settings(inConfig(TemplateTest)(Defaults.testSettings): _*)
    .configs(IntegrationTest)
    .settings(inConfig(TemplateItTest)(Defaults.itSettings): _*)
    .settings(
      Keys.fork in IntegrationTest := false,
      unmanagedSourceDirectories in IntegrationTest <<= (baseDirectory in IntegrationTest)(base => Seq(base / "it")),
      addTestReportOption(IntegrationTest, "int-test-reports"),
      testGrouping in IntegrationTest := oneForkedJvmPerTest((definedTests in IntegrationTest).value),
      parallelExecution in IntegrationTest := false)
    .settings(resolvers ++= Seq(Resolver.bintrayRepo("hmrc", "releases"), Resolver.jcenterRepo))
    .enablePlugins(SbtDistributablesPlugin, SbtAutoBuildPlugin, SbtGitVersioning)
}

private object TestPhases {

  val allPhases = "tt->test;test->test;test->compile;compile->compile"
  val allItPhases = "tit->it;it->it;it->compile;compile->compile"

  lazy val TemplateTest = config("tt") extend Test
  lazy val TemplateItTest = config("tit") extend IntegrationTest

  def oneForkedJvmPerTest(tests: Seq[TestDefinition]) =
    tests map {
      test => new Group(test.name, Seq(test), SubProcess(ForkOptions(runJVMOptions = Seq("-Dtest.name=" + test.name))))
    }
}