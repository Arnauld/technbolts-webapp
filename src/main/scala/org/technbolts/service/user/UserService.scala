package org.technbolts.service.user

import org.technbolts.model.user.User
import org.technbolts.util.{CommandContext, Command}
import org.slf4j.LoggerFactory

class UserService {

}

class CreateUserCommand(val user:User) extends Command[User] {

  private val logger = LoggerFactory.getLogger(classOf[CreateUserCommand])

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