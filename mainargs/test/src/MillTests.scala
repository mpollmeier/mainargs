package mainargs
import utest._


object MillTests extends TestSuite{
  implicit def MapParser[K: ArgReader, V: ArgReader] = new ArgReader[Map[K, V]](
    "k=v",
    strs => {
      strs.foldLeft[Either[String, Map[K, V]]](Right(Map())){
        case (Left(s), _) => Left(s)
        case (Right(prev), token) =>
          token.split("=", 2) match{
            case Array(k, v) =>
              for {
                tuple <- Right((k, v)): Either[String, (String, String)]
                (k, v) = tuple
                key <- implicitly[ArgReader[K]].read(Seq(k))
                value <- implicitly[ArgReader[V]].read(Seq(v))
              }yield prev + (key -> value)

            case _ => Left("parameter must be in k=v format")
          }
      }
    }
  )
  """
Mill Build Tool
usage: mill [mill-options] [target [target-options]]

  -c, --code           Pass in code to be run immediately in the REPL
  -h, --home           The home directory of the REPL; where it looks for config and caches
  -p, --predef         Lets you load your predef from a custom location, rather than the
                       default location in your Ammonite home
  --no-default-predef  Disable the default predef and run Ammonite with the minimal predef
                       possible
  -s, --silent         Make ivy logs go silent instead of printing though failures will
                       still throw exception
  --help               Print this message
  --color              Enable or disable colored output; by default colors are enabled
                       in both REPL and scripts if the console is interactive, and disabled
                       otherwise
  -w, --watch          Watch and re-run your scripts when they change
  --bsp                Run a BSP server against the passed scripts
  --thin               Hide parts of the core of Ammonite and some of its dependencies. By default, the core of
                       Ammonite and all of its dependencies can be seen by users from the Ammonite session. This
                       option mitigates that via class loader isolation.
  --repl               Run Mill in interactive mode and start a build REPL. In this mode, no mill server will be used. Must be the first argument.
  --no-server          Run Mill in interactive mode, suitable for opening REPLs and taking user input. In this mode, no mill server will be used. Must be the first argument.
  -i, --interactive    Run Mill in interactive mode, suitable for opening REPLs and taking user input. In this mode, no mill server will be used. Must be the first argument.
  -v, --version        Show mill version and exit.
  --disable-ticker     Disable ticker log (e.g. short-lived prints of stages and progress bars)
  -d, --debug          Show debug output on STDOUT
  -k, --keep-going     Continue build, even after build failures
  -D, --define         Define (or overwrite) a system property
  -j, --jobs           Allow processing N targets in parallel. Use 1 to disable parallel and 0 to use as much threads as available processors.
  -b, --bell           Ring the bell once if the run completes successfully, twice if it fails.
"""
  implicit object PathRead extends ArgReader[os.Path]("path", strs => Right(os.Path(strs.head, os.pwd)))
  @main(
    name = "Mill Build Tool",
    doc = "usage: mill [mill-options] [target [target-options]]")
  case class Config(
     @arg(
      flag = true,
      doc = "Run Mill in interactive mode and start a build REPL. In this mode, no mill server will be used. Must be the first argument.")
    repl: Boolean = false,
    @arg(
      name = "no-server",
      flag = true,
      doc = "Run Mill in interactive mode, suitable for opening REPLs and taking user input. In this mode, no mill server will be used. Must be the first argument.")
    noServer: Boolean = false,
    @arg(
      short = 'i',
      flag = true,
      doc = "Run Mill in interactive mode, suitable for opening REPLs and taking user input. In this mode, no mill server will be used. Must be the first argument.")
    interactive: Boolean = false,
    @arg(
      short = 'v',
      flag = true,
      doc = "Show mill version and exit.")
    version: Boolean = false,
    @arg(
      name = "bell",
      short = 'b',
      flag = true,
      doc = "Ring the bell once if the run completes successfully, twice if it fails.")
    ringBell: Boolean = false,
    @arg(
      name = "disable-ticker",
      flag = true,
      doc = "Disable ticker log (e.g. short-lived prints of stages and progress bars)")
    disableTicker: Boolean = false,
    @arg(
      short = 'd',
      flag = true,
      doc = "Show debug output on STDOUT")
    debug: Boolean = false,
    @arg(
      name = "keep-going",
      short = 'k',
      flag = true,
      doc = "Continue build, even after build failures")
    keepGoing: Boolean = false,
    @arg(
      name = "define",
      short = 'D',
      doc = "Define (or overwrite) a system property")
    extraSystemProperties: Map[String, String] = Map(),
    @arg(name = "jobs",
      short = 'j',
      doc = "Allow processing N targets in parallel. Use 1 to disable parallel and 0 to use as much threads as available processors.")
    threadCount: Int = 1
  )

  val tests = Tests {

    val parser = ParserForClass[Config]

    test("formatMainMethods"){
      parser.helpText()

    }
    test("parseInvoke"){
      parser.constructEither(Array("--jobs", "12")) ==>
        Right(Config(threadCount = 12))
    }
  }
}
