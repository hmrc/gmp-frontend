import sbt._

object FrontendBuild extends Build with MicroService {
  val appName = "gmp-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val playHealthVersion = "2.0.0"
  private val frontendBootstrapVersion = "7.11.0"
  private val govukTemplateVersion = "5.0.0"
  private val playUiVersion = "5.4.0"
  private val playAuthFrontendVersion = "6.2.0"
  private val playConfigVersion = "3.1.0"
  private val playGraphiteVersion = "3.1.0"
  private val domainVersion = "4.0.0"
  private val httpCachingVersion = "6.1.0"
  private val logbackJsonLoggerVersion = "3.1.0"
  private val taxyearVersion = "0.3.0"
  private val playPartialsVersion = "5.2.0"
  private val emailAddressVersion = "2.0.0"
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
    "uk.gov.hmrc" %% "play-graphite" % playGraphiteVersion,
    "uk.gov.hmrc" %% "logback-json-logger" % logbackJsonLoggerVersion,
    "uk.gov.hmrc" %% "tax-year" % taxyearVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "emailaddress" % emailAddressVersion,
    "uk.gov.hmrc" %% "bulk-entity-streaming" % bulkEntityStreamingVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = Seq.empty
  }

  private val scalatestPlusPlayVersion = "1.5.1"
  private val scalatestVersion = "2.2.6"
  private val pegdownVersion = "1.6.0"
  private val jsoupVersion = "1.9.2"
  private val mockitoCoreVersion = "1.9.5"

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalatestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % jsoupVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-core" % mockitoCoreVersion %scope
      )
    }.test
  }

  private val hmrcTestVersion = "2.2.0"

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalatestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalatestPlusPlayVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % jsoupVersion % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}
