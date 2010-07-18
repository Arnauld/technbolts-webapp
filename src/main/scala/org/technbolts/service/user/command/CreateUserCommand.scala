package org.technbolts.service.user.command

import org.springframework.beans.factory.annotation.{Qualifier, Autowired, Configurable}
import org.technbolts.util.{CommandContext, Command}
import org.technbolts.service.user.UserRepository
import org.technbolts.domain.user.User
import org.slf4j.LoggerFactory

@Configurable
class CreateUserCommand(val user:User) extends Command[User] {

  private val logger = LoggerFactory.getLogger(classOf[CreateUserCommand])
  
  var userRepository:UserRepository = _
  @Autowired
  @Qualifier("userRepository")
  def setUserRepository(userRepository:UserRepository) = {
    this.userRepository = userRepository
  }

  def getCommandType = classOf[CreateUserCommand]

  def execute(context: CommandContext) = {
    context.get(classOf[User]) match {
      case Some(user:User) =>
      case _ => logger.info("No user to create")
    }
  }

  def initialize(context: CommandContext) = {
    context.set(classOf[User], user)
  }
}