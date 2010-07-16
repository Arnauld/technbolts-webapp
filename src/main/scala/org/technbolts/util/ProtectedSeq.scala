package org.technbolts.util

trait ProtectedSeq[T] {
  @volatile private var items: List[T] = Nil
  @volatile private var frozen = false

  private def safe_?(f: => Any) {
    frozen match {
      case false => f
      case _ => throw new IllegalStateException("Cannot modify once frozen.");
    }
  }

  def toList = items

  def prepend(r: T): ProtectedSeq[T] = {
    safe_? {
      items = r :: items
    }
    this
  }

  def remove(f: T => Boolean) {
    safe_? {
      items = items.remove(f)
    }
  }

  def append(r: T): ProtectedSeq[T] = {
    safe_? {
      items = items ::: List(r)
    }
    this
  }

  protected def freeze:Unit = frozen=true
  def isFrozen = frozen
}