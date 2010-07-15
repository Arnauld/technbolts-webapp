package org.technbolts.service.cluster

import net.liftweb.common.Full
import net.liftweb.http._
import org.technbolts.protobuf.ClusterPBO
import org.technbolts.protobuf.ClusterPBO.{NodeInfo, SearchNodeInfo}

object ClusterApiService {
  def dispatch: LiftRules.DispatchPF = {
    case request @ Req("api"::"cluster"::"node_info"::Nil, _ , reqType) => () => Full(nodeInfo(request, reqType))
    case request @ Req("api"::"cluster"::"node_ping"::Nil, _ , reqType) => () => Full(nodePing(request, reqType))
    case request @ Req("api"::"cluster"::"node_join"::Nil, _ , reqType) => () => Full(nodeJoin(request, reqType))
    case request @ Req("api"::"cluster"::"node_leave"::Nil, _ , reqType) => () => Full(nodeLeave(request, reqType))
    case request @ Req("api"::"cluster"::"node_status"::Nil, _ , reqType) => () => Full(nodeStatus(request, reqType))
    case request @ Req("api"::"cluster"::"node_update"::Nil, _ , reqType) => () => Full(nodeUpdate(request, reqType))
  }

  def nodeInfo(req:Req, reqType:RequestType):LiftResponse = {
    val search : SearchNodeInfo = ClusterPBO.SearchNodeInfo.parseFrom(req.request.inputStream)
    OkResponse()
  }

  def nodePing(req:Req, reqType:RequestType):LiftResponse = {
    OkResponse()
  }

  def nodeJoin(req:Req, reqType:RequestType):LiftResponse = {
    OkResponse()
  }

  def nodeLeave(req:Req, reqType:RequestType):LiftResponse = {
    OkResponse()
  }

  def nodeStatus(req:Req, reqType:RequestType):LiftResponse = {
    OkResponse()
  }

  def nodeUpdate(req:Req, reqType:RequestType):LiftResponse = {
    OkResponse()
  }
}