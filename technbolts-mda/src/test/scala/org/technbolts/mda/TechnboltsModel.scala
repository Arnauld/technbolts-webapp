package org.technbolts.mda

import annotation.{ProtobufField, ProtobufMessage, Protobuf}
import org.junit.Test

class TechnboltsModelTest {
  @Test
  def useCaseEx1:Unit = {
    val model = GeneratorModel("org.technbolts")
    model.models.append(classOf[Request])
    model.generate
  }
}

@ProtobufMessage(message = "request")
class Request {

  @ProtobufField
  object id;
}