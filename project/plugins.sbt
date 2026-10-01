resolvers += "HMRC Releases" at "https://artefacts.tax.service.gov.uk/artifactory/hmrc-releases/"
resolvers += "Typesafe Releases" at "https://repo.typesafe.com/typesafe/releases/"
resolvers += "HMRC-open-artefacts-maven" at "https://open.artefacts.tax.service.gov.uk/maven2"
resolvers += Resolver.url("HMRC-open-artefacts-ivy", url("https://open.artefacts.tax.service.gov.uk/ivy2"))(Resolver.ivyStylePatterns)
resolvers += Resolver.jcenterRepo

addSbtPlugin("uk.gov.hmrc"        %  "sbt-auto-build"        % "3.24.0")
addSbtPlugin("uk.gov.hmrc"        %  "sbt-distributables"    % "2.6.0")
addSbtPlugin("org.playframework"  %  "sbt-plugin"            % "3.0.10" exclude("org.slf4j", "slf4j-simple"))
addSbtPlugin("org.scoverage"      %  "sbt-scoverage"         % "2.3.0")
addSbtPlugin("org.scalameta"      %  "sbt-scalafmt"          % "2.5.2")
addSbtPlugin("uk.gov.hmrc"        %  "sbt-sass-compiler"     % "0.13.0")
