package com.evolution.infrastructure.utils

object Utils {
  def log(value: String): Unit                     = println(value)
  def logDebug(value: String): Unit                = println(value)
  def validate(value: String): Option[String]      = Some(value)
  def parse(value: String): Unit                   = println(value)
  def extractId(value: String): String             = value
  def findSomething(value: String): Option[String] = None
  def valueFrom(value: String): String             = value
}
