// Copyright (C) 2017 Anduin Transactions, Inc.

package anduin.exception

private[anduin] abstract class BaseException extends Exception {
  def message: String
  def cause: Throwable = null // scalastyle:ignore null
  final override def getMessage: String = message
  final override def getCause: Throwable = cause
  final override def fillInStackTrace(): Throwable = this
}
