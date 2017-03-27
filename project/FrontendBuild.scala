import sbt._

object FrontendBuild extends Build with MicroService {
  val appName = "gmp-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val playHealthVersion = "2.1.0"
  private val frontendBootstrapVersion = "7.22.0"
  private val govukTemplateVersion = "5.1.0"
  private val playUiVersion = "7.0.0"
  private val playAuthFrontendVersion = "6.3.0"
  private val playConfigVersion = "4.3.0"
  private val playGraphiteVersion = "3.2.0"
  private val domainVersion = "4.1.0"
  private val httpCachingVersion = "6.2.0"
  private val logbackJsonLoggerVersion = "3.1.0"
  private val taxyearVersion = "0.3.0"
  private val playPartialsVersion = "5.3.0"
  private val emailAddressVersion = "2.0.0"
  private val bulkEntityStreamingVersion = "1.0.0"

  private val scalatestPlusPlayVersion = "1.5.1"
  private val scalatestVersion = "2.2.6"
  private val pegdownVersion = "1.6.0"
  private val jsoupVersion = "1.9.2"
  private val mockitoCoreVersion = "1.9.5"
  private val hmrcTestVersion = "2.3.0"

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
