import sbt.Keys._
import sbt._

object Build extends Build {

  val compileNXJ = TaskKey[Unit]("compileNXJ","Compiles for NXJ")

  val linkNXJ = TaskKey[Unit]("linkNXJ","Links compiled NXJ program")

  val uploadNXJ = TaskKey[Unit]("uploadNXJ","Uploads linked NXJ program")

  val uploadRunNXJ = TaskKey[Unit]("uploadRunNXJ","Uploads and runs linked NXJ program")

  val debugNXJ = inputKey[Unit]("Debugs using given debug numbers.")

  val nxjWin = TaskKey[Unit]("nxjWin","Compile link and upload nxt program on dumb operating systems.")

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

  def listJavaFiles(root:File):List[String] = {
    root.listFiles()
      .filter(f => (f.isFile && f.getName.toLowerCase.endsWith(".java")) || f.isDirectory)
      .foldLeft[List[String]](Nil)((files,file) => {
        if(file.isDirectory){
          listJavaFiles(file) ::: files
        }else{
          file.getCanonicalPath :: files
        }
    })
  }

  val nxjCompileDir = file("target") / "nxj"

  /**
   * NXT only project.
   * Implementation of NXT bot Controller.
   */
  lazy val nxtController = Project("nxt",file("nxt"),settings = sharedSettings ++ Seq(
    unmanagedBase := file("lejos") / "lib" / "nxt", //Only nxt controller can depend on nxt libs! They are not on pc!
    mainClass := Some("lego.nxt.bootstrap.RandomBootstrap"),
    nxjWin := {
      //TODO Make this thing working even on Bill Gate's OS
      val parameters = (listJavaFiles(file("shared") / "src" / "main" / "java") ::: listJavaFiles(file("nxt") / "src" / "main" / "java"))
        .addString(new StringBuilder("-d . -source 6 -target 6 ")," ").toString()
      Process("..\\..\\lejos\\bin\\nxjcw.bat "+parameters,nxjCompileDir).!

      Process(s"..\\..\\lejos\\bin\\nxjlinkw.bat -v -od linkDump -o NxtProgram.nxj ${mainClass.value}",nxjCompileDir).!

      Process(s"..\\..\\lejos\\bin\\nxjuploadw.bat -r NxtProgram.nxj",nxjCompileDir).!
    },
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
    },
    debugNXJ := {
      import sbt.complete.DefaultParsers._
      val args = spaceDelimited("<arg>").parsed

      s"./debugNXJ.sh ${args.addString(new StringBuilder," ")}".!
    })
    ++ addCommandAlias("nxj",";compileNXJ;linkNXJ;uploadRunNXJ")
  ) dependsOn shared

  /**
   * PC only project.
   * Implementation of bot Controller that is used when dry-testing Bots.
   */
  lazy val simulatorController = Project("simulator",file("simulator"),settings = sharedSettings ++ Seq(
    autoScalaLibrary := true,
    mainClass := Some("lego.simulator.ui.UIMain"),
    libraryDependencies += "com.google.guava" % "guava" % "18.0"
    //TODO add dependency on pc lejos stuff
  )) dependsOn shared

}
