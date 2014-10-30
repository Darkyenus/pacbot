import sbt._
import sbt.Keys._

object Build extends Build {

  val compileNXJ = TaskKey[Unit]("compileNXJ","Compiles for NXJ")

  val linkNXJ = TaskKey[Unit]("linkNXJ","Links compiled NXJ program")

  val uploadNXJ = TaskKey[Unit]("uploadNXJ","Uploads linked NXJ program")

  val uploadRunNXJ = TaskKey[Unit]("uploadRunNXJ","Uploads and runs linked NXJ program")

  val sharedSettings = Seq(
    crossPaths := false,
    autoScalaLibrary := false,
    javacOptions ++= Seq(
      "-target", "6",
      "-source", "6",
      "-Xlint:deprecation"
    )
  )

  /**
   * Universal project
   * Contains api that Bot-s and Controller-s implement.
   *
   * Also contains Bot implementations and loose Controller definitions, that are completely implemented in controller projects.
   */
  lazy val shared = Project("shared",file("shared"),settings = sharedSettings ++ Seq(
  ))

  /**
   * NXT only project.
   * Implementation of NXT bot Controller.
   */
  lazy val nxtController = Project("nxt",file("nxt"),settings = sharedSettings ++ Seq(
    unmanagedBase := file("lejos") / "lib" / "nxt", //Only nxt controller can depend on nxt libs! They are not on pc!
    mainClass := Some("lego.nxt.bootstrap.RandomBootstrap"),
    compileNXJ := {
      "./compileNXJ.sh".!
    },
    linkNXJ := {
      s"./linkNXJ.sh ${mainClass.value.getOrElse(sys.error("Specify mainClass for nxtController first."))}".!
    },
    uploadNXJ := {
      "./uploadNXJ.sh -u".!
    },
    uploadRunNXJ := {
      "./uploadNXJ.sh -r -u".!
    })
    ++ addCommandAlias("nxj",";compileNXJ;linkNXJ;uploadRunNXJ")
  ) dependsOn shared

  /**
   * PC only project.
   * Implementation of bot Controller that is used when dry-testing Bots.
   */
  lazy val simulatorController = Project("simulator",file("simulator"),settings = sharedSettings ++ Seq(
    autoScalaLibrary := true,
    mainClass := Some("lego.simulator.ui.UIMain")
    //TODO add dependency on pc lejos stuff
  )) dependsOn shared

}