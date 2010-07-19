package org.technbolts.util

import collection.mutable.{ListBuffer, HashMap}
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.{Qualifier, Autowired, Configurable}

class CommandException extends Exception

object Message {
  val INFO  = 100;
  val WARN  = 200;
  val ERROR = 300;

  def apply(messageText:String) {
    new Message() {
      override def text = messageText
    }
  }
}

trait Message {
  def text:String = null
  def exception:Exception = null
  def level:Int = Message.INFO
}

trait Command[R] {
  def getCommandType:Class[_]
  def initialize(context:CommandContext):Unit
  def execute(context:CommandContext):Unit
  var result:Option[R] = None
  val messages = new ListBuffer[Message]
}

class ContextFrozenException extends Exception

class CommandContext {
  private val context = new HashMap[Any,Any]
  private var frozen = false

  def get(key:Any):Option[Any] = context.get(key)
  def isFrozen = frozen
  def set(key:Any,value:Any):Unit = {
    if(frozen)
      throw new ContextFrozenException
    context.put(key,value)
  }
}

trait Interceptor {
  def intercept(chain:CommandInvoker[_]):Unit
}

trait InterceptorsProvider {
  def getInterceptors(command:Command[_]):List[Interceptor]
}

@Configurable
class CommandInvoker[R](val command:Command[R]) {

  private val logger = LoggerFactory.getLogger(classOf[CommandInvoker])

  var interceptorsProvider:InterceptorsProvider = _
  @Autowired
  @Qualifier("interceptorsProvider")
  def setInterceptorsProvider(interceptorsProvider:InterceptorsProvider) = {
    this.interceptorsProvider = interceptorsProvider
  }

  val context = new CommandContext
  private var interceptors: Iterator[Interceptor] = null

  def invoke:Unit = {
    if(interceptors==null) {
      interceptors =  if(interceptorsProvider!=null)
                        interceptorsProvider.getInterceptors(command)
                      else
                        Iterator.empty
      command.initialize(context)
    }

    if(interceptors.hasNext)
      interceptors.next.intercept(this)
    else
      // no more interceptor, call the command
      command.execute(context)
  }
}