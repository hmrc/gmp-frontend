import sbt._

object FrontendBuild extends Build with MicroService {
  import scala.util.Properties.envOrElse

  val appName = "gmp-frontend"
  val appVersion = envOrElse("GMP_FRONTEND_VERSION", "999-SNAPSHOT")

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.PlayImport._
  import play.core.PlayVersion

  private val playHealthVersion = "1.1.0"
  private val frontendBootstrapVersion = "6.7.0"
  private val govukTemplateVersion = "4.0.0"
  private val playUiVersion = "4.16.0"
  private val playAuthFrontendVersion = "5.5.0"
  private val playConfigVersion = "2.1.0"
  private val metricsPlayVersion = "0.2.1"
  private val metricsGraphiteVersion = "3.0.2"
  private val domainVersion = "3.7.0"
  private val httpCachingVersion = "5.6.0"
  private val playJsonLogger = "2.1.1"
  private val taxyearVersion = "0.2.0"
  private val playPartialsVersion = "4.5.0"
  private val emailAddressVersion = "1.1.0"
  private val bulkEntityStreamingVersion = "1.0.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "frontend-bootstrap" % frontendBootstrapVersion,
    "uk.gov.hmrc" %% "play-health" % playHealthVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "uk.gov.hmrc" %% "play-config" % playConfigVersion,
    "uk.gov.hmrc" %% "govuk-template" % govukTemplateVersion,
    "uk.gov.hmrc" %% "play-authorised-frontend" % playAuthFrontendVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingVersion,
    "com.kenshoo" %% "metrics-play" % metricsPlayVersion,
    "com.codahale.metrics" % "metrics-graphite" % metricsGraphiteVersion,
    "uk.gov.hmrc" %% "play-json-logger" % playJsonLogger,
    "uk.gov.hmrc" %% "tax-year" % taxyearVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "emailaddress" % emailAddressVersion,
    "uk.gov.hmrc" %% "bulk-entity-streaming" % bulkEntityStreamingVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = ???
  }

  private val scalatestVersion = "2.2.6"
  private val scalatestPlusPlayVersion = "1.2.0"
  private val pegdownVersion = "1.6.0"
  private val jsoupVersion = "1.9.2"

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalatestVersion % scope,
        "org.scalatestplus" %% "play" % scalatestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % jsoupVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope
      )
    }.test
  }

  private val hmrcTestVersion = "1.8.0"

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalatestVersion % scope,
        "org.scalatestplus" %% "play" % scalatestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % jsoupVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}
