package org.technbolts.service.user.command

import org.springframework.beans.factory.annotation.{Qualifier, Autowired, Configurable}
import org.technbolts.util.{CommandContext, Command}
import org.technbolts.service.user.UserRepository
import org.technbolts.domain.user.User
import org.slf4j.LoggerFactory
import org.technbolts.protobuf.UserPBModel
import UserPBModel.{User => PBUser}

@Configurable
class CreateUserCommand(val user:PBUser) extends Command[User] {

  private val logger = LoggerFactory.getLogger(classOf[CreateUserCommand])

  var userRepository:UserRepository = _
  @Autowired
  @Qualifier("userRepository")
  def setUserRepository(userRepository:UserRepository) = {
    this.userRepository = userRepository
  }

  def getCommandType = classOf[CreateUserCommand]

  def execute(context: CommandContext) = {
    context.get(classOf[PBUser]) match {
      case Some(pbUser:PBUser) => {
        val user = User.fromPBUser(pbUser)
        val saved = userRepository.save(user)
        result = Some(saved)
      }
      case _ => logger.info("No user data defined")
    }
  }

  def initialize(context: CommandContext) = {
    context.set(classOf[PBUser], user)
  }
}