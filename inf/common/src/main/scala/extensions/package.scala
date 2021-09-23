package ru.otus.sales.leads.generator.inf.common

package object extensions {
  implicit class StringOpts(val value: String) extends AnyVal {
    def isNullOrEmpty = value == null || value.isEmpty
  }

  implicit class ListOpts[T](val value: T) extends AnyVal {
    def unary_~ = ::(value, Nil)
  }

}
