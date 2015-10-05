name := "twitter-streaming"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % "1.4.0",
  "org.apache.spark"  %% "spark-streaming" % "1.4.0",
  "org.apache.spark"  %% "spark-streaming-twitter" % "1.4.0",
  "com.typesafe" % "config" % "1.2.1"
)