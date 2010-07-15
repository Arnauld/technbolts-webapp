package org.technbolts.model

import common.HasLabels
import collection.mutable.HashMap

case class Feature(val name:String,val active:Boolean) extends HasLabels

trait HasFeatures {
  private var features = HashMap[String,Feature]()
  def getFeatures = features.values

  def hasFeature(name:String):Boolean = features.get(name).isDefined
  def isFeatureActive(name:String):Boolean = features.get(name) match {
    case Some(f:Feature) => f.active
    case _ => false
  }
  def updateFeature(feature:Feature):Unit = {
    features.put(feature.name, feature)
  }
}