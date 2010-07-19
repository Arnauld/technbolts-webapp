package org.technbolts.service.user

import command.CreateUserCommand
import scala.collection.JavaConversions._
import net.liftweb.common.Full
import net.liftweb.http._
import org.slf4j.{Logger, LoggerFactory}
import org.technbolts.protobuf.{UserPBModel, UserPBCommand}
import UserPBModel.{User => PBUser}
import UserPBCommand._

class UserApiService

object UserApiService {

  private val logger:Logger = LoggerFactory.getLogger(classOf[UserApiService])


  def dispatch: LiftRules.DispatchPF = {
    case request @ Req("api"::"user"::"create"::Nil, _ , reqType) => () => Full(createUser(request, reqType))
    case request @ Req("api"::"user"::"delete"::Nil, _ , reqType) => () => Full(deleteUser(request, reqType))
    case request @ Req("api"::"user"::"search"::Nil, _ , reqType) => () => Full(searchUser(request, reqType))
    case request @ Req("api"::"user"::_, _ , reqType)  => () => Full(NotAcceptableResponse())
  }

  def createUser(req:Req, reqType:RequestType):LiftResponse = {
    logger.info("API: Creating user")

    val response = CreateUserResponse.newBuilder

    val cmdDef = CreateUser.parseFrom(req.request.inputStream)
    for(user <- cmdDef.getUserList()) {
      val cmd = new CreateUserCommand(user)
      cmd.invoke
    }

    OkResponse()
  }

  def deleteUser(req:Req, reqType:RequestType):LiftResponse = {
    logger.info("API: Deleting user")
    OkResponse()
  }

  def searchUser(req:Req, reqType:RequestType):LiftResponse = {
    logger.info("API: Searching user")
    OkResponse()
  }
}