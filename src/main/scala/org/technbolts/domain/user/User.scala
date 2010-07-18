package org.technbolts.domain.user

import org.springframework.beans.factory.annotation.Configurable
import org.slf4j.{Logger, LoggerFactory}
import org.technbolts.protobuf.UserPBModel.{User=>PBUser}
import org.technbolts.domain.{HasId, HasIdImpl}

object User {
  def fromPBUser(pbo:PBUser) = {
    val user = new User
    user.replacePBUser(pbo)
    user
  }
}

@Configurable
class User extends HasId[String] with HasIdImpl[String] {
  private val logger: Logger = LoggerFactory.getLogger(classOf[User])
  private var pbUser:Option[PBUser] = None

  def replacePBUser(pbUser:PBUser):Unit = {
    logger.debug("Internal PBUser replaced by: {}", pbUser)
    this.pbUser = if(pbUser==null)
                    None
                  else
                    Some(pbUser)
  }
  
  def asPBUser = {
    val uuid = getId.getOrElse(null)

    pbUser match {
      case None => PBUser.newBuilder.setUuid(uuid).build
      case Some(pb) => {
        if(uuid == pb.getUuid)
          pbUser;
        else
          PBUser.newBuilder(pb).setUuid(uuid).build
      }
    }
  }
}
