// Copyright (C) 2017 Anduin Transactions, Inc.

package ngthanhtrung.graphql.codegen.cli

final case class CodegenCliException(
  message: String,
  cause: Throwable = null // scalastyle:ignore null
) extends Exception {
  override def getMessage: String = message
  override def getCause: Throwable = cause
  override def fillInStackTrace(): Throwable = this
}

object CodegenCliException {

  def apply(cause: Throwable): CodegenCliException = {
    CodegenCliException(cause.getMessage, cause)
  }
}
