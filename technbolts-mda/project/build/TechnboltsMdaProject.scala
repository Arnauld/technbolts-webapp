import java.io.File
import java.util.jar.Attributes
import java.util.jar.Attributes.Name._
import sbt._
import sbt.CompileOrder._

class TechnboltsMdaProject(info: ProjectInfo) extends DefaultProject(info) {

  // -------------------------------------------------------------------------------------------------------------------
  // Compile settings
  // -------------------------------------------------------------------------------------------------------------------

  override def compileOptions = super.compileOptions ++
    Seq("-deprecation",
        "-Xmigration",
        "-Xcheckinit",
        //"-Xstrict-warnings",
        "-Xwarninit",
        "-encoding", "utf8")
        .map(x => CompileOption(x))
  override def javaCompileOptions = JavaCompileOption("-Xlint:unchecked") :: super.javaCompileOptions.toList

  // -------------------------------------------------------------------------------------------------------------------
  // Deploy/dist settings
  // -------------------------------------------------------------------------------------------------------------------

  lazy val deployPath = info.projectPath / "deploy"
  lazy val distPath = info.projectPath / "dist"
  def distName = "%s_%s-%s.zip".format(name, buildScalaVersion, version)

  // -------------------------------------------------------------------------------------------------------------------
	// All repositories *must* go here! See ModuleConigurations below.
  // -------------------------------------------------------------------------------------------------------------------

	object Repositories {
	  lazy val AkkaRepo             = MavenRepository("Akka Repository", "http://scalablesolutions.se/akka/repository")
	  lazy val CodehausSnapshotRepo = MavenRepository("Codehaus Snapshots", "http://snapshots.repository.codehaus.org")
	  lazy val EmbeddedRepo         = MavenRepository("Embedded Repo", (info.projectPath / "embedded-repo").asURL.toString)
	  lazy val FusesourceSnapshotRepo = MavenRepository("Fusesource Snapshots", "http://repo.fusesource.com/nexus/content/repositories/snapshots")
	  lazy val GuiceyFruitRepo      = MavenRepository("GuiceyFruit Repo", "http://guiceyfruit.googlecode.com/svn/repo/releases/")
	  lazy val JBossRepo            = MavenRepository("JBoss Repo", "https://repository.jboss.org/nexus/content/groups/public/")
	  lazy val JavaNetRepo          = MavenRepository("java.net Repo", "http://download.java.net/maven/2")
	  lazy val SonatypeSnapshotRepo = MavenRepository("Sonatype OSS Repo", "http://oss.sonatype.org/content/repositories/releases")
	  lazy val SunJDMKRepo          = MavenRepository("Sun JDMK Repo", "http://wp5.e-taxonomy.eu/cdmlib/mavenrepo")
	}

  // -------------------------------------------------------------------------------------------------------------------
  // ModuleConfigurations
  // Every dependency that cannot be resolved from the built-in repositories (Maven Central and Scala Tools Releases)
  // must be resolved from a ModuleConfiguration. This will result in a significant acceleration of the update action.
  // Therefore, if repositories are defined, this must happen as def, not as val.
  // -------------------------------------------------------------------------------------------------------------------

	import Repositories._
  lazy val atmosphereModuleConfig  = ModuleConfiguration("org.atmosphere", SonatypeSnapshotRepo)
  lazy val grizzlyModuleConfig     = ModuleConfiguration("com.sun.grizzly", JavaNetRepo)
  lazy val guiceyFruitModuleConfig = ModuleConfiguration("org.guiceyfruit", GuiceyFruitRepo)
  // lazy val hawtdispatchModuleConfig  = ModuleConfiguration("org.fusesource.hawtdispatch", FusesourceSnapshotRepo)
  lazy val jbossModuleConfig       = ModuleConfiguration("org.jboss", JBossRepo)
  lazy val jdmkModuleConfig        = ModuleConfiguration("com.sun.jdmk", SunJDMKRepo)
  lazy val jerseyContrModuleConfig = ModuleConfiguration("com.sun.jersey.contribs", JavaNetRepo)
  lazy val jerseyModuleConfig      = ModuleConfiguration("com.sun.jersey", JavaNetRepo)
  lazy val jgroupsModuleConfig     = ModuleConfiguration("jgroups", JBossRepo)
  lazy val jmsModuleConfig         = ModuleConfiguration("javax.jms", SunJDMKRepo)
  lazy val jmxModuleConfig         = ModuleConfiguration("com.sun.jmx", SunJDMKRepo)
  lazy val liftModuleConfig        = ModuleConfiguration("net.liftweb", ScalaToolsSnapshots)
  lazy val multiverseModuleConfig  = ModuleConfiguration("org.multiverse", CodehausSnapshotRepo)
  lazy val nettyModuleConfig       = ModuleConfiguration("org.jboss.netty", JBossRepo)
  lazy val scalaTestModuleConfig   = ModuleConfiguration("org.scalatest", ScalaToolsSnapshots)
  lazy val embeddedRepo            = EmbeddedRepo // This is the only exception, because the embedded repo is fast!

  // -------------------------------------------------------------------------------------------------------------------
  // Versions
  // -------------------------------------------------------------------------------------------------------------------

  lazy val ATMO_VERSION       = "0.6"
  lazy val CAMEL_VERSION      = "2.4.0"
  lazy val CASSANDRA_VERSION  = "0.6.1"
	lazy val DispatchVersion    = "0.7.4"
  lazy val HAWTDISPATCH_VERSION = "1.0"
	lazy val JacksonVersion     = "1.2.1"
  lazy val JERSEY_VERSION     = "1.2"
  lazy val LIFT_VERSION       = "2.0-scala280-SNAPSHOT"
  lazy val MULTIVERSE_VERSION = "0.6-SNAPSHOT"
  lazy val SCALATEST_VERSION  = "1.2-for-scala-2.8.0.final-SNAPSHOT"
	lazy val Slf4jVersion       = "1.6.0"
  lazy val SPRING_VERSION     = "3.0.3.RELEASE"
	lazy val WerkzVersion       = "2.2.1"

  // -------------------------------------------------------------------------------------------------------------------
  // Dependencies
  // -------------------------------------------------------------------------------------------------------------------

  object Dependencies {

    // Compile

    lazy val commons_codec = "commons-codec" % "commons-codec" % "1.4" % "compile"
    lazy val commons_io = "commons-io" % "commons-io" % "1.4" % "compile"
    lazy val commons_logging = "commons-logging" % "commons-logging" % "1.1.1" % "compile"

    lazy val jsr166x = "jsr166x" % "jsr166x" % "1.0" % "compile"
    lazy val jsr250 = "javax.annotation" % "jsr250-api" % "1.0" % "compile"
    lazy val jsr311 = "javax.ws.rs" % "jsr311-api" % "1.1" % "compile"

    lazy val log4j = "log4j" % "log4j" % "1.2.15" % "compile"
    lazy val netty = "org.jboss.netty" % "netty" % "3.2.1.Final" % "compile"
    lazy val protobuf = "com.google.protobuf" % "protobuf-java" % "2.3.0" % "compile"

    lazy val slf4j       = "org.slf4j" % "slf4j-api"     % Slf4jVersion % "compile"
    lazy val slf4j_log4j = "org.slf4j" % "slf4j-log4j12" % Slf4jVersion % "compile"

    lazy val spring_beans   = "org.springframework" % "spring-beans"   % SPRING_VERSION % "compile"
    lazy val spring_context = "org.springframework" % "spring-context" % SPRING_VERSION % "compile"

    // Test

    lazy val google_coll    = "com.google.collections" % "google-collections"  % "1.0"             % "test"
    lazy val jettyServer    = "org.mortbay.jetty"      % "jetty"               % "6.1.22"          % "test"
    lazy val junit          = "junit"                  % "junit"               % "4.8.1"           % "test"
    lazy val mockito        = "org.mockito"            % "mockito-all"         % "1.8.4"           % "test"
    lazy val hamcrest       = "org.hamcrest"           % "hamcrest-all"        % "1.1"             % "test"

    lazy val scalatest      = "org.scalatest"          % "scalatest"           % SCALATEST_VERSION % "test"
  }

  val slf4j          = Dependencies.slf4j
  val slf4j_log4j    = Dependencies.slf4j_log4j
  val log4j          = Dependencies.log4j
  val protobuf       = Dependencies.protobuf
  val spring_beans   = Dependencies.spring_beans
  val spring_context = Dependencies.spring_context
  //
  val junit          = Dependencies.junit
  val scalatest      = Dependencies.scalatest

}
