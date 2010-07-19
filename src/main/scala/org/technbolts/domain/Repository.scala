package org.technbolts.domain

import collection.mutable.{HashMap}

object SearchResult {
  def apply[T](results:Iterable[T]) = {
    new SearchResult[T] {
      val count = Some(items.size)
      val items = results.iterator
    }
  }
}

trait SearchResult[T] {
  val count:Option[Int]
  val items:Iterator[T]
}

trait Repository[T] {
  def save(item:T):T
  def delete(item:T):Option[T]
  def search(sample:T,template:SearchTemplate):SearchResult[T]
}

trait MemoryRepository[K,T <: HasId[K]] extends Repository[T] {
  val items = new HashMap[K,T]

  def toFilter(sample:T,template:SearchTemplate): (T)=>Boolean
  def generateId:K

  def save(item:T):T = {
    item.getId match {
      case None =>
        val key = generateId
        item.setId(key)
        items.put(key, item)
      case Some(key) => // already have an id
        items.put(key, item)
    }
    item
  }

  def delete(item:T):Option[T] = {
    item.getId match {
      case Some(key) => items.remove(key)
      case _ => //Cannot remove item
        None
    }
  }

  def search(sample:T,template:SearchTemplate):SearchResult[T] = {
    val found = items.values.filter(toFilter(sample,template))
    SearchResult(found)
  }
}