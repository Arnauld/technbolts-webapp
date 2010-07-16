package org.technbolts.util

import collection.mutable.{ListBuffer, HashMap}

class CommandException extends Exception

object Message {
  val INFO  = 100;
  val WARN  = 200;
  val ERROR = 300;

  def apply(text:String) {
    new Message() {
      override def text = text
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

class CommandInvoker[R](val command:Command[R]) extends ProtectedSeq[Interceptor] {

  val context = new CommandContext
  private var interceptorIter: Iterator[Interceptor] = null

  def invoke:Unit = {
    if(interceptorIter==null) {
      freeze
      interceptorIter = toList.iterator
      command.initialize(context)
    }

    if(interceptorIter.hasNext)
      interceptorIter.next.intercept(this)
    else
      // no more interceptor, call the command
      command.execute(context)
  }
}