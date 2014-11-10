import com.google.common.base.Charsets
import com.google.common.io.Files
import sbt.Keys._
import sbt._

object Build extends Build {

  val compileNXJ = TaskKey[Unit]("compileNXJ","Compiles for NXJ")

  val linkNXJ = TaskKey[Unit]("linkNXJ","Links compiled NXJ program")

  val uploadNXJ = TaskKey[Unit]("uploadNXJ","Uploads linked NXJ program")

  val uploadRunNXJ = TaskKey[Unit]("uploadRunNXJ","Uploads and runs linked NXJ program")

  val debugNXJ = inputKey[Unit]("Debugs using given debug numbers.")

  val nxw = TaskKey[Unit]("nxw","Compiles, links and uploads program on dumb operating systems.")

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

  val nxtCompileFolder = file("target") / "nxt"
  val nxwBat = nxtCompileFolder / "nxw.bat"

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
    },
    debugNXJ := {
      import sbt.complete.DefaultParsers._
      val args = spaceDelimited("<arg>").parsed

      s"./debugNXJ.sh ${args.addString(new StringBuilder," ")}".!
    },
    nxw := {
      def listJavaFiles(root:File):List[String] = {
        root.listFiles().foldLeft[List[String]](Nil)((sources,file) => {
          if(file.isDirectory){
            listJavaFiles(file) ::: sources
          }else if(file.isFile && file.getName.toLowerCase.endsWith(".java")){
            file.getCanonicalPath :: sources
          }else{
            sources
          }
        })
      }

      val sourceFiles = (listJavaFiles(file("shared") / "src" / "main" / "java") ::: listJavaFiles(file("shared") / "src" / "main" / "java"))
        .addString(new StringBuilder," ").toString()

      val PROGRAM_NAME = "NXWPRogram"

      val batContent =
      s"""..\..\lejos\bin\nxjcw -d . -source 6 -target 6 $sourceFiles""" + "\r\n" +
      s"""..\..\lejos\bin\nxjlinkw -v -od linkDump -o $PROGRAM_NAME.nxj ${mainClass.value}""" + "\r\n" +
      s"""..\..\lejos\bin\nxjuploadw -r $PROGRAM_NAME.nxj"""

      nxtCompileFolder.mkdirs()
      Files.write(batContent,nxwBat,Charsets.UTF_8)
      Process(nxwBat.getName,nxtCompileFolder,("","")).!
    }
  )
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
