package org.technbolts.service.api

import net.liftweb.http.LiftRules
import org.technbolts.service.entity.EntityApiService
import org.technbolts.service.cluster.ClusterApiService
import org.technbolts.service.user.UserApiService

object ApiService {
  val ContentType = "application/octet-stream+protobuf"

  def dispatchList: List[LiftRules.DispatchPF] =
    List(
      RootApiService.dispatch,
      UserApiService.dispatch,
      EntityApiService.dispatch,
      ClusterApiService.dispatch)
}

trait ApiService {
  
}