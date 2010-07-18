package org.technbolts.service.user

import org.technbolts.domain.user.User
import org.technbolts.domain.{SearchTemplate, MemoryRepository, Repository}
import org.slf4j.{LoggerFactory, Logger}
import org.technbolts.util.UUIDGenerator

trait UserRepository extends Repository[User] {
}

class MemoryUserRepository extends UserRepository with MemoryRepository[String,User] {

  private val logger: Logger = LoggerFactory.getLogger(classOf[MemoryUserRepository])

  def generateId = UUIDGenerator.generate

  def toFilter(sample: User, template: SearchTemplate) = {
    if(sample.hasId)
      (u:User) => {
        u.getId.getOrElse(null)==sample.getId.getOrElse(null)
      }
    else {
      logger.warn("Filter supports only id, 'false' filter returned")
      (u:User) => {
        false
      }
    }
  }
}