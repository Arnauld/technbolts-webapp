package org.technbolts.service.api

import ApiService.ContentType
import net.liftweb.common.Full
import org.technbolts.protobuf.UserPBModel.{User => PBUser}
import net.liftweb.http._

object RootApiService {

  def dispatch: LiftRules.DispatchPF = {
    case request @ Req("api"::Nil, _ , reqType) => () => Full(handleCall(request, reqType))
  }

  def handleCall(req:Req, reqType:RequestType):LiftResponse = {
    val input = req.request.inputStream
    val userRead = PBUser.parseFrom(input)

    val userWritten = PBUser.newBuilder(userRead).build

    var data: Array[Byte] = userWritten.toByteArray
    val headers = List(
        ("Content-type" -> ContentType)
       //,("Content-length" -> data.length.toString)
      )
    StreamingResponse(
      new java.io.ByteArrayInputStream(data),
      () => {},
      data.length,
      headers, Nil, 200)
  }
}
