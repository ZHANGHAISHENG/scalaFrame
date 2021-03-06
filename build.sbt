name := "projJson"

version := "0.1"

scalaVersion := "2.12.4"

val catsV = "1.0.0-RC1"
val circeV = "0.9.0-M2"
val finagleV = "17.10.0"
val scroogeV = "17.10.0"
val akkaHttpV = "10.0.11"
val akkaV = "2.5.19"
val slickV = "3.2.1"
val prometheusV = "0.1.0"

libraryDependencies ++= Seq(
    "org.typelevel"                %% "cats-core"                  % catsV
  , "org.typelevel"                %% "cats-free"                  % catsV
  , "io.circe"                     %% "circe-core"                 % circeV
  , "io.circe"                     %% "circe-generic"              % circeV
  , "io.circe"                     %% "circe-parser"               % circeV
  , "io.circe"                     %% "circe-generic-extras"       % circeV
  , "io.circe"                     %% "circe-optics"               % circeV
  , "org.apache.thrift"            %  "libthrift"                  % "0.9.3"
  , "com.twitter"                  %% "scrooge-core"               % scroogeV exclude("com.twitter", "libthrift")
  , "com.twitter"                  %% "finagle-thrift"             % finagleV exclude("com.twitter", "libthrift")
  , "com.twitter"                  %% "finagle-thriftmux"          % finagleV exclude("com.twitter", "libthrift")
  , "com.twitter"                  %% "finagle-http"               % finagleV exclude("com.twitter", "libthrift")
  , "com.twitter"                  %% "finagle-tunable"            % finagleV exclude("com.twitter", "libthrift")
  , "com.typesafe.akka"            %% "akka-slf4j"                 % akkaV
  , "com.typesafe.akka"            %% "akka-actor"                 % akkaV
  , "com.typesafe.akka"            %% "akka-stream"                % akkaV
  , "com.typesafe.akka"            %% "akka-persistence"           % akkaV
  , "com.typesafe.akka"            %% "akka-stream"                % akkaV
  , "com.typesafe.akka"            %% "akka-http-core"             % akkaHttpV
  , "com.typesafe.akka"            %% "akka-http"                  % akkaHttpV
  , "com.typesafe.akka"            %% "akka-stream-kafka"          % "0.18"
  , "io.prometheus"                %  "simpleclient"               % prometheusV
  , "io.prometheus"                %  "simpleclient_hotspot"       % prometheusV
  , "io.prometheus"                %  "simpleclient_common"        % prometheusV
  , "io.prometheus"                %  "simpleclient_pushgateway"   % prometheusV
  , "com.typesafe.slick"           %% "slick"                      % slickV
  , "com.typesafe.slick"           %% "slick-hikaricp"             % slickV
  , "com.typesafe.slick"           %% "slick-codegen"              % slickV
  , "org.postgresql"               %  "postgresql"                 % "42.1.4"
  , "org.scalatest"                %% "scalatest"                  % "3.0.4"
  , "org.specs2"                   %% "specs2-core"                % "4.0.2"        % "test"
  , "com.typesafe.akka"            %% "akka-testkit"               % akkaV
  , "com.h2database"               %  "h2"                         % "1.4.196"
  , "com.typesafe.akka"            %% "akka-stream-testkit"        % akkaV
  , "com.typesafe.akka"            %% "akka-http-testkit"          % akkaHttpV
  , "com.github.tminglei"          %% "slick-pg"                   % "0.16.0"
  , "com.typesafe.akka"            %% "akka-testkit"               % "2.5.9"
  , "com.aerospike"                %  "aerospike-client"           % "4.0.7"
  , "org.bouncycastle"             %  "bcprov-jdk15on"             % "1.59"
  /*, "com.zzcm"                     %  "surgesms"                   % "1.0.3"*/
  , "org.slf4j"                    %  "log4j-over-slf4j"           % "1.7.25"
)
libraryDependencies += "org.fusesource.leveldbjni" % "leveldbjni-all" % "1.8"
libraryDependencies += "org.iq80.leveldb" % "leveldb" % "0.9"
libraryDependencies += "com.typesafe.akka" %% "akka-persistence-query" % "2.5.19"


coverageEnabled := true
coverageFailOnMinimum := true
coverageMinimum := 90
coverageOutputCobertura := false
coverageOutputXML := false

