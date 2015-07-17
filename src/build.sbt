import com.typesafe.sbt.SbtStartScript

scalaVersion := "2.11.0"

libraryDependencies ++= Seq(
	"org.scala-lang.modules" %% "scala-parser-combinators" % "1.0.1",
	"org.yaml" % "snakeyaml" % "1.15"
)

scalaSource in Compile := baseDirectory.value / "source"

sourcesInBase := false

// 143 chars is the filename length limit in eCryptfs, commonly used in linux distros to encrypt homedirs.
// Make scala respect that limit via max-classfile-name, or compilation fails.
scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xmax-classfile-name", "143")

libraryDependencies += "com.github.scopt" %% "scopt" % "3.2.0"

seq(SbtStartScript.startScriptForClassesSettings: _*)
