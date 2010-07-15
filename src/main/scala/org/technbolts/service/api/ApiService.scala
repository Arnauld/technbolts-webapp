package org.technbolts.service.api

import net.liftweb.http.LiftRules

object ApiService {
  val ContentType = "application/octet-stream+protobuf"

  def dispatchList: List[LiftRules.DispatchPF] =
    List(
      RootApiService.dispatch,
      EntityApiService.dispatch,
      ClusterApiService.dispatch)
}

trait ApiService {
  
}