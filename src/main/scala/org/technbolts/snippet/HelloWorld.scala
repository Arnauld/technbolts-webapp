package org.technbolts {
package snippet {

import _root_.scala.xml.NodeSeq
import _root_.net.liftweb.util.Helpers
import Helpers._
import org.springframework.beans.factory.annotation.{Qualifier, Autowired, Configurable}
import util.ExecutionService

@Configurable
class HelloWorld {

  var executionService:ExecutionService = null
  @Autowired
  @Qualifier("sharedExecutionService")
  def setExecutionService(executionService:ExecutionService) = {
    this.executionService = executionService
    println("Execution service set: "+executionService)
  }

  def howdy(in: NodeSeq): NodeSeq =
    Helpers.bind("b", in, "time" -> (new _root_.java.util.Date).toString)
}

}
}