package org.technbolts.mda.protobuf

trait ProtobufGeneratorPlugin {
  def postScan(generator:ProtobufGenerator):Unit
}