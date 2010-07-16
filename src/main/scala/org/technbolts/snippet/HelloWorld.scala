package org.technbolts.snippet

import _root_.scala.xml.NodeSeq

import _root_.net.liftweb.common._
import _root_.net.liftweb.util._
import _root_.net.liftweb.util.Helpers
import Helpers._
import org.springframework.beans.factory.annotation.{Qualifier, Autowired, Configurable}
import org.technbolts.util.TimeService
import org.slf4j.{Logger, LoggerFactory}
import org.technbolts.model.user.User

@Configurable
class HelloWorld {
  private val logger: Logger = LoggerFactory.getLogger(classOf[HelloWorld])

  var timeService:TimeService = null
  @Autowired
  @Qualifier("timeService")
  def setTimeService(timeService:TimeService) = {
    this.timeService = timeService
  }

  def howdy(in: NodeSeq): NodeSeq = {
    logger.debug("howdy !");
    val user:String = User.currentUser match {
      case Full(user) => user.email
      case _ => "anonymous"
    }
    Helpers.bind("b", in, "time" -> timeService.getFormattedTime, "user" -> user)
  }
}
