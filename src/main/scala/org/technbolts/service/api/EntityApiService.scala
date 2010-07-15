package org.technbolts.service.api

import net.liftweb.common.Full
import net.liftweb.http._

object EntityApiService {

  def dispatch: LiftRules.DispatchPF = {
    case request @ Req("api"::"entity"::"create"::Nil, _ , reqType) => () => Full(createEntity(request, reqType))
    case request @ Req("api"::"entity"::"delete"::Nil, _ , reqType) => () => Full(deleteEntity(request, reqType))
    case request @ Req("api"::"entity"::"search"::Nil, _ , reqType) => () => Full(searchEntity(request, reqType))
    case request @ Req("api"::"entity"::command::Nil, _ , reqType)  => () => Full(unknownCommand(request, command, reqType))
  }

  def createEntity(req:Req, reqType:RequestType):LiftResponse = {
    OkResponse()
  }

  def deleteEntity(req:Req, reqType:RequestType):LiftResponse = {
    OkResponse()
  }

  def searchEntity(req:Req, reqType:RequestType):LiftResponse = {
    OkResponse()
  }

  def unknownCommand(req:Req, command:String, reqType:RequestType):LiftResponse = {
    NotAcceptableResponse()
  }
}