// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen

import java.io.File

import sangria.ast.{AstVisitor, Document, Field}
import sangria.introspection.TypeNameMetaField
import sangria.schema.{AbstractType, Schema}
import sangria.visitor.VisitorCommand

// scalastyle:off underscore.import
import cats.implicits._
// scalastyle:on underscore.import

private[codegen] final class DocumentTransformer(schema: Schema[_, _]) {

  import DocumentTransformer._ // scalastyle:ignore import.grouping underscore.import

  def transform(
    document: Document,
    sourceFile: Option[File]
  ): Result[Document] = {
    Either.catchOnly[CodegenException] {
      AstVisitor.visitAstWithTypeInfo(schema, document) { typeInfo =>
        AstVisitor({
          case field: Field =>
            val tpe = typeInfo.tpe.getOrElse(TypeNotAvailableException(field, sourceFile))

            tpe match {
              case _: AbstractType =>
                val selections = field.selections
                val hasTypenameField = selections.exists {
                  case subfield: Field if subfield.name == TypeNameMetaField.name => true
                  case _ => false
                }

                if (!hasTypenameField) {
                  VisitorCommand.Transform(field.copy(selections = selections :+ TypenameField))
                } else {
                  VisitorCommand.Continue
                }

              case _ =>
                VisitorCommand.Continue
            }
        })
      }
    }
  }
}

private[codegen] object DocumentTransformer {

  private val TypenameField = Field(
    alias = None,
    name = TypeNameMetaField.name,
    arguments = Vector.empty,
    directives = Vector.empty,
    selections = Vector.empty
  )
}
