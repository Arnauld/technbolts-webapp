package org.technbolts.service.user

import net.liftweb.common.Full
import net.liftweb.http._

object UserApiService {

  def dispatch: LiftRules.DispatchPF = {
    case request @ Req("api"::"user"::"create"::Nil, _ , reqType) => () => Full(createUser(request, reqType))
    case request @ Req("api"::"user"::"delete"::Nil, _ , reqType) => () => Full(deleteUser(request, reqType))
    case request @ Req("api"::"user"::"search"::Nil, _ , reqType) => () => Full(searchUser(request, reqType))
    case request @ Req("api"::"user"::_, _ , reqType)  => () => Full(NotAcceptableResponse())
  }

  def createUser(req:Req, reqType:RequestType):LiftResponse = {
    OkResponse()
  }

  def deleteUser(req:Req, reqType:RequestType):LiftResponse = {
    OkResponse()
  }

  def searchUser(req:Req, reqType:RequestType):LiftResponse = {
    OkResponse()
  }
}