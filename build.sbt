organization := "com.github.biopet"
organizationName := "Sequencing Analysis Support Core - Leiden University Medical Center"

startYear := Some(2017)

name := "replacecontigsvcffile"
biopetUrlName := "replacecontigsvcffile"

biopetIsTool := true

// TODO: Check if mainClass is correct
mainClass in assembly := Some("nl.biopet.tools.replacecontigsvcffile.ReplaceContigsVcfFile")

developers := List(
  Developer(id="ffinfo", name="Peter van 't Hof", email="pjrvanthof@gmail.com", url=url("https://github.com/ffinfo"))
)

scalaVersion := "2.11.11"

libraryDependencies += "com.github.biopet" %% "tool-utils" % "0.2"
libraryDependencies += "com.github.biopet" %% "tool-test-utils" % "0.1" % Test
libraryDependencies += "com.github.biopet" %% "ngs-utils" % "0.1"