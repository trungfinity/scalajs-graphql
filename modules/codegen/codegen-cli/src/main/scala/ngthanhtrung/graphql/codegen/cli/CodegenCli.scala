package ngthanhtrung.graphql.codegen.cli

import caseapp.{CommandAppWithPreCommand, RemainingArgs}

object CodegenCli extends CommandAppWithPreCommand[CodegenBeforeCommand, CodegenCommand] {

  def beforeCommand(options: CodegenBeforeCommand, remainingArgs: Seq[String]): Unit = {
    if (remainingArgs.nonEmpty) {
      System.err.println(s"Found extra arguments: ${remainingArgs.mkString(" ")}.")
      sys.exit(255) // scalastyle:ignore magic.number
    }
  }

  def run(command: CodegenCommand, args: RemainingArgs): Unit = {
    command.run(args).left.foreach {
      case exception: CodegenCliException =>
        System.err.println(exception.getMessage)
        sys.exit(1)

      case throwable: Throwable =>
        System.err.println(
          "An unhandled error occurred." +
            "\n\nPlease include the stack trace below and report the issue at" +
            " https://github.com/ngthanhtrung/scala-graphql/issues" +
            "\n"
        )
        throwable.printStackTrace(System.err)
        sys.exit(1)
    }
  }
}
