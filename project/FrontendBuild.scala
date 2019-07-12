import sbt._

object FrontendBuild extends Build with MicroService {
  val appName = "gmp-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.core.PlayVersion
  import play.sbt.PlayImport._

  private val frontendBootstrapVersion = "12.9.0"
  private val domainVersion = "5.6.0-play-25"
  private val httpCachingVersion = "8.4.0-play-25"
  private val taxyearVersion = "0.5.0"
  private val playPartialsVersion = "6.9.0-play-25"
  private val emailAddressVersion = "3.2.0"
  private val bulkEntityStreamingVersion = "1.0.0"

  private val scalatestPlusPlayVersion = "2.0.1"
  private val scalatestVersion = "3.0.2"
  private val pegdownVersion = "1.6.0"
  private val jsoupVersion = "1.11.3"
  private val mockitoCoreVersion = "1.9.5"
  private val hmrcTestVersion = "3.4.0-play-25"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "frontend-bootstrap" % frontendBootstrapVersion,
    "uk.gov.hmrc" %% "domain" % domainVersion,
    "uk.gov.hmrc" %% "http-caching-client" % httpCachingVersion,
    "uk.gov.hmrc" %% "tax-year" % taxyearVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "emailaddress" % emailAddressVersion,
    "uk.gov.hmrc" %% "bulk-entity-streaming" % bulkEntityStreamingVersion,
    "uk.gov.hmrc" %% "auth-client"  %  "2.22.0-play-25"
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = Seq.empty
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
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
