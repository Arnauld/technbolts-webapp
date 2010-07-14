package org.technbolts.service

import org.springframework.beans.factory.annotation.Configurable
import net.liftweb.common.Full
import org.technbolts.protobuf.UserPBO
import net.liftweb.http._

object ApiService {

  val ContentType = "application/octet-stream+protobuf"

  def dispatch: LiftRules.DispatchPF = {
    case request @ Req("api"::Nil, _ , PutRequest) =>
      () => Full(handleCall(request))
  }

  def handleCall(req:Req):LiftResponse = {
    val input = req.request.inputStream
    val userRead = UserPBO.User.parseFrom(input)
    val userWritten = UserPBO.User.newBuilder(userRead).addEmail("john@do.e").build

    var data: Array[Byte] = userWritten.toByteArray
    val headers = List(
        ("Content-type" -> ContentType),
        ("Content-length" -> data.length.toString))
    StreamingResponse(
      new java.io.ByteArrayInputStream(data),
      () => {},
      data.length,
      headers, Nil, 200)
  }
}

@Configurable
class ApiService {

}