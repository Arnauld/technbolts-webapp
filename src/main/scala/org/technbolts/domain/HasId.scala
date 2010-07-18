package org.technbolts.domain

trait HasId[K] {
  def getId:Option[K]
  def hasId:Boolean = getId.isDefined
  def setId(k:K):Unit
}

trait HasIdImpl[K] {
  private var id:Option[K] = None
  def getId:Option[K] = id
  def setId(id:K):Unit = { this.id = Some(id) }
}
