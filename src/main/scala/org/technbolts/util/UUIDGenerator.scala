package org.technbolts.util

import java.util.UUID

object UUIDGenerator {
  private var singleton = new UUIDGenerator {}
  def get = singleton
  def set(generator:UUIDGenerator):Unit = { singleton=generator }
  def generate:String = singleton.generate
}

trait UUIDGenerator {
  def generate:String = UUID.randomUUID.toString
}