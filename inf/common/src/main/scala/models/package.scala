package ru.otus.sales.leads.generator.inf.common

import sttp.model.StatusCode

package object models {
  case class ErrorInfo[T](msg: String, statusCode: StatusCode, details: List[T] = Nil)
      extends Exception

  type AuthId = Int
}
