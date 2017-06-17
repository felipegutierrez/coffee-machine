name := """coffee-machine"""

version := "1.0"

scalaVersion := "2.11.6"

val sparkVersion = "1.6.1"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.3.11",
  "com.typesafe.akka" %% "akka-testkit" % "2.3.11" % "test",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5",
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-streaming-twitter" % sparkVersion)


resolvers ++= Seq(
 "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

resolvers += Resolver.sonatypeRepo("releases")

