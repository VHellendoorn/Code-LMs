package ls

import sbt.{ BuiltinCommands, CommandStrings, Def, EvaluateConfigurations,
            LineRange, Load, Project, SessionSettings, State }

trait Declarations {

  def resolverDeclarations(l: LibraryVersions, v: Version) =
    (v.resolvers.filterNot(sbtBuiltinResolver).zipWithIndex.map {
      case (url, i) =>
        "resolvers += \"%s\" at \"%s\"".format(
          "%s-resolver-%s".format(l.name, i),
          url
        )
    })

  def sbtPluginDeclaration(l: LibraryVersions, v: Version) =
     ("""addSbtPlugin("%s" %% "%s" %% "%s")""" format(
       l.organization /*v.organization*/, l.name, v.version)
    ).trim

  def libraryDeclaration(l: LibraryVersions, v: Version, config: Option[String]) =
    ("""libraryDependencies += "%s" %%%% "%s" %% "%s"%s""" format(
      l.organization/*v.organization*/, l.name, v.version, config match {
        case Some(c) => """ %% "%s" """.format(c)
        case _ => ""
      }
    )).trim

  // todo: use sbt constants here
  private def sbtBuiltinResolver(s: String) =
    s.contains("http://repo1.maven.org/maven2/") || s.contains("http://scala-tools.org/repo-releases")

}

/** Attempts to package what is needed to capture and persist settings,
 *  namely libraryDependencies, within sbt's state */
object Depends extends Declarations {

  def install(state: State, persistently: Boolean)(libLines: Seq[String]) = {
    val extracted = Project extract state
		import extracted.{ currentLoader, currentRef, rootProject, session, structure  }
    import BuiltinCommands.{ imports, reapply, DefaultBootCommands }
		import CommandStrings.{ DefaultsCommand, InitCommand }

    /** mix in new setting(s) returnning a new session */
    def mix(ses: SessionSettings, line: String,
            settings: Seq[Def.Setting[_]]) =
      ses.appendSettings( settings map (a => (a, line.split('\n').toList)))

    /** evaluate a line of settings for the current session */
    def eval(ses: SessionSettings, line: String) =
      EvaluateConfigurations.evaluateSetting(
        ses.currentEval(), "<set>", imports(extracted), line, LineRange(0, 0)
      )(currentLoader)

    /** Transforms provided settings with existing settings and return a new set of settings */
    def transform(settings: Seq[Def.Setting[_]]) =
       Load.transformSettings(
         Load.projectScope(currentRef), currentRef.build, rootProject, settings)

    // relevant https://github.com/harrah/xsbt/pull/369

    // eval the first line to obtain
    // a starting point for a fold            
    val (first, rest) = (libLines.head, libLines.tail)        
		val settings = eval(session, first)
		val append = transform(settings)
		val session0 = mix(session, first, append)

    // fold over lines to build up a new setting including all
    // setting appendings
    val (_, _, newestSession) = ((settings, append, session0) /: rest)((a, line) =>
      a match {
        case (set, app, ses) =>
          val settings = eval(ses, line)
		      val append = transform(settings)
          (settings, append, mix(ses, line, append))
      })

		val commands = DefaultsCommand +: InitCommand +: DefaultBootCommands
		reapply(newestSession, structure,
            if(persistently) state.copy(
              remainingCommands = (CommandStrings.SessionCommand + " save") +:
                commands)
            else state)
  }

  /** Attemps to `try` or `install` a give library dependency
   *  with an optional version and ivy config descriptor string */
  def apply(state: State, ls: Seq[LibraryVersions], version: Option[String],
            config: Option[String], persistently: Boolean): State = {
    
    val log = state.log

    // what's going on below is simply building up a pipeline of settings
    // to ship off to sbt's to eval facitlity, then reload and optional save
    // the settings in the project's .sbt build definition
    val extracted = Project extract state
		//import extracted._
    import BuiltinCommands.{ imports, reapply, DefaultBootCommands }
		import CommandStrings.{ DefaultsCommand, InitCommand }

    // one or more lines consisting of libraryDependency and resolvers
    // lhs is (plugin config, plugin instruct) | rhs is library config
    val lines: Either[(Seq[String], String), Seq[String]] = if(ls.size > 1) {
      sys.error("""More than one libraries were resolved.
                | Try speficying the library as :user/:repo/:library-name to disambiguate.""".stripMargin)
    } else {
      val l = ls(0)
      (version match {
        case Some(v) => l.versions.find(_.version.equalsIgnoreCase(v))
        case _ => Some(l.versions(0))
      }) match {
        case Some(v) =>                
          if(l.sbt) {
            log.info("Discovered sbt plugin %s@%s" format(l.name, v.version))
            val pluginDef = sbtPluginDeclaration(l, v) +: resolverDeclarations(l, v)
            Left((pluginDef, PluginSupport.help(l, v, pluginDef)))                  
          } else {
            log.info("Discovered library %s@%s" format(l.name, v.version))
            // detect dep
            Conflicts.detect(extracted.get(sbt.Keys.libraryDependencies), l, v)
            log.info(
              if(persistently) "Evaluating and installling %s@%s" format(l.name, v.version)
              else "Evalutating %s@%s. Enter `session clear` to revert" format(
                l.name, v.version
              ))
            Right(libraryDeclaration(l, v, config) +: resolverDeclarations(l, v))
          }

        case _ => sys.error("Could not find %s version of this library. possible versions (%s)" format(
          version.getOrElse("latest"), l.versions.mkString(", "))
        )
      }
    }

    // currently, we do not support plugin installation.
    // when we do, that magic should happen for `Left`s
    lines.fold({
      case (lines, help) =>
        log.info(help)
        state
    }, install(state, persistently))
  }
}
