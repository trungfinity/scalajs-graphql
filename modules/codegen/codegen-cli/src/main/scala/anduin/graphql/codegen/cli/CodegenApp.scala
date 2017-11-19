// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen.cli

import caseapp.{CommandAppWithPreCommand, RemainingArgs}

object CodegenApp extends CommandAppWithPreCommand[CodegenBeforeCommand, CodegenCommand] {

  def beforeCommand(options: CodegenBeforeCommand, remainingArgs: Seq[String]): Unit = {
    if (remainingArgs.nonEmpty) {
      Console.err.println(s"Found extra arguments: ${remainingArgs.mkString(" ")}.")
      sys.exit(255) // scalastyle:ignore magic.number
    }
  }

  def run(command: CodegenCommand, args: RemainingArgs): Unit = {
    command.run(args)
  }
}
