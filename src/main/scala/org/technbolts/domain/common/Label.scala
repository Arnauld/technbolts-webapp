package org.technbolts.domain.common

import collection.mutable.HashMap

case class Label(val locale:String,val value:String)

trait HasLabels {

  private var labels = HashMap[String,Label]()
  def getLabels = labels.values

  def getLabel(locale:String):Option[String] = labels.get(locale) match {
    case Some(label:Label) => Some(label.value)
    case _ => None
  }

  def hasLabel(locale:String):Boolean = labels.get(locale) match {
    case Some(label:Label) => label.value!=null
    case _ => false
  }

  def setLabel(label:Label):Unit = {
    labels.put(label.locale,label)
  }
  
  def setLabel(locale:String,value:String):Unit = setLabel(Label(locale,value))

  def setLabels(newLabels:HasLabels):Unit = {
    newLabels.getLabels.foreach { l => setLabel(l) }
  }

}