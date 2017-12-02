// Copyright (C) 2017 Anduin Transactions, Inc.

package ngthanhtrung.graphql.codegen.cli

import java.io.{File, FileNotFoundException}

import scala.io.{Codec, Source}

import io.circe.ParsingFailure
import io.circe.parser.parse
import org.http4s.client.blaze.PooledHttp1Client
import sangria.ast.Document
import sangria.parser.{DeliveryScheme, QueryParser, SyntaxError}
import sangria.schema.{Schema, SchemaMaterializationException}

// scalastyle:off underscore.import
import cats.implicits._
import org.http4s._
import sangria.marshalling.circe._
// scalastyle:on underscore.import

object DocumentReader {

  private val client = PooledHttp1Client()

  private def sourceFromFile(file: File, fileType: String): Result[String] = {
    Either
      .catchNonFatal(
        Source.fromFile(file)(Codec.UTF8).mkString
      )
      .recoverWith {
        case exception: FileNotFoundException =>
          Left(
            CodegenCliException(
              s"${fileType.capitalize} file doesn't exist: ${file.getAbsolutePath}.",
              exception
            )
          )
      }
  }

  private def fromGraphqlSource(source: String): Result[Document] = {
    QueryParser.parse(source)(DeliveryScheme.Either).recoverWith {
      case error: SyntaxError => Left(CodegenCliException(error))
    }
  }

  def fromGraphqlFile(file: File): Result[Document] = {
    for {
      source <- sourceFromFile(file, "document")
      document <- fromGraphqlSource(source)
    } yield document
  }

  private def schemaFromDocument(document: Document): Result[Schema[_, _]] = {
    Either
      .catchNonFatal(
        Schema.buildFromAst(document)
      )
      .recoverWith {
        case exception: SchemaMaterializationException =>
          Left(CodegenCliException(exception))
      }
  }

  private def schemaFromGraphqlSource(source: String): Result[Schema[_, _]] = {
    for {
      document <- fromGraphqlSource(source)
      schema <- schemaFromDocument(document)
    } yield schema
  }

  private def schemaFromJsonSource(source: String): Result[Schema[_, _]] = {
    for {
      json <- parse(source).recoverWith {
        case failure: ParsingFailure =>
          Left(CodegenCliException(failure))
      }

      schema <- Either
        .catchNonFatal(
          Schema.buildFromIntrospection(json)
        )
        .recoverWith {
          case exception: SchemaMaterializationException =>
            Left(CodegenCliException(exception))
        }
    } yield schema
  }

  def schemaFromGraphqlFile(file: File): Result[Schema[_, _]] = {
    for {
      source <- sourceFromFile(file, "schema")
      schema <- schemaFromGraphqlSource(source)
    } yield schema
  }

  def schemaFromJsonFile(file: File): Result[Schema[_, _]] = {
    for {
      source <- sourceFromFile(file, "schema")
      schema <- schemaFromJsonSource(source)
    } yield schema
  }

  def schemaFromIntrospectionUri(
    uriString: String,
    headers: List[(String, String)]
  ): Result[Schema[_, _]] = {
    for {
      uri <- (Uri.fromString(uriString): Either[Throwable, Uri]).recoverWith {
        case failure: ParseFailure =>
          Left(CodegenCliException(failure.sanitized, failure))
      }

      source <- client
        .fetchAs[String](
          Request(
            uri = uri,
            headers = Headers(
              (headers ++ List(
                "Accept" -> "application/json",
                "Content-Type" -> "application/json"
              )).map {
                case (name, value) => Header(name, value)
              }
            )
          )
        )
        .unsafeAttemptRun()

      schema <- schemaFromJsonSource(source)
    } yield schema
  }
}
