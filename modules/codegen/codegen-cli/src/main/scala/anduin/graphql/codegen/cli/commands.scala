// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen.cli

import java.io.{File, FileNotFoundException, PrintStream}

import scala.meta.prettyprinters.Syntax

import sangria.ast
import sangria.validation.QueryValidator

import anduin.graphql.codegen.CodeGenerator
import anduin.graphql.codegen.parse.DocumentParser

// scalastyle:off underscore.import
import caseapp._
import cats.implicits._
// scalastyle:on underscore.import

@AppName("GraphQL code generator")
@AppVersion("0.1.0")
@ProgName("codegen-cli")
final case class CodegenBeforeCommand()

sealed abstract class CodegenCommand extends Product with Serializable {
  def run(args: RemainingArgs): Result[Unit]
}

@CommandName("gen-ops")
final case class GenerateOperations(
  @Name("s") schema: String,
  @Name("p") `package`: Option[String],
  @Name("o") output: Option[String]
) extends CodegenCommand { self =>

  private[this] def outputStream(filename: String): Result[PrintStream] = {
    output.fold[Result[PrintStream]](
      Right(System.out)
    ) { output =>
      val dir = `package`.fold(output) { `package` =>
        s"$output/${`package`.replace('.', File.separatorChar)}"
      }

      val path = s"$dir${File.separator}$filename"
      val file = new File(path)

      file.getParentFile.mkdirs()

      Either
        .catchNonFatal(
          new PrintStream(file)
        )
        .recoverWith {
          case exception: FileNotFoundException =>
            Left(
              CodegenCliException(
                s"File $path or one of its parent directory doesn't exist.",
                exception
              )
            )

          case exception: SecurityException =>
            Left(CodegenCliException(s"Could not write to file $path.", exception))
        }
    }
  }

  def run(args: RemainingArgs): Result[Unit] = {
    for {
      schema <- DocumentReader.schemaFromGraphqlFile(new File(self.schema))
      parser = new DocumentParser(schema)

      _ <- args.args.toVector.foldLeftM[Result, Unit](()) { (_, path) =>
        for {
          document <- DocumentReader.fromGraphqlFile(new File(path))

          _ <- {
            val violations = QueryValidator.default.validateQuery(schema, document)

            if (violations.nonEmpty) {
              val aggregatedMessage = violations.map(_.errorMessage).mkString("\n\n")
              Left(CodegenCliException(aggregatedMessage))
            } else {
              Right.apply(())
            }
          }

          operations <- {
            parser.parse(document).leftMap { exception =>
              CodegenCliException(exception)
            }
          }

          _ <- operations.foldLeftM[Result, Unit](()) { (_, operation) =>
            // Remember to fix this hack later
            val filenameSuffix = operation.operationType match {
              case ast.OperationType.Query => "Query"
              case ast.OperationType.Mutation => "Mutation"
              case ast.OperationType.Subscription => "Subscription"
            }

            for {
              outputStream <- self.outputStream(
                s"${operation.name.capitalize}$filenameSuffix.scala"
              )
            } yield {
              val ast = CodeGenerator.generate(operation, `package`)
              val source = ast.show[Syntax]
              outputStream.println(source)
              outputStream.close()
            }
          }
        } yield ()
      }
    } yield ()
  }
}
