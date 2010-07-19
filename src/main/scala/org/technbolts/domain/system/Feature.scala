package org.technbolts.domain.system

import collection.mutable.HashMap
import org.technbolts.domain.common.HasLabels

object Feature {
  def apply(name:String) = new Feature(name,false)
}

case class Feature(val name:String,val enabled:Boolean) extends HasLabels {
  def enable:Feature = enable(true)
  def disable:Feature = enable(false)

  def enable(enabled:Boolean):Feature = {
    if(this.enabled==enabled)
      this
    else {
      val newFeature = new Feature(name,enabled)
      newFeature.setLabels(this)
      newFeature
    }
  }
}

trait HasFeatures {
  private var features = HashMap[String,Feature]()
  def getFeatures = features.values

  def hasFeature(name:String):Boolean = features.get(name).isDefined
  def isFeatureEnabled(name:String):Boolean = features.get(name) match {
    case Some(f:Feature) => f.enabled
    case _ => false
  }
  def updateFeature(feature:Feature):Unit = {
    features.put(feature.name, feature)
  }
}