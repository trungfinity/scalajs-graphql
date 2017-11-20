// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.graphql.codegen.cli

import anduin.exception.BaseException

final case class CodegenCliException(
  message: String,
  override val cause: Throwable = null // scalastyle:ignore null
) extends BaseException

object CodegenCliException {

  def apply(cause: Throwable): CodegenCliException = {
    CodegenCliException(cause.getMessage, cause)
  }
}
