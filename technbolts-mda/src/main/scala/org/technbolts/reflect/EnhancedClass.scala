package org.technbolts.reflect

import java.lang.annotation.{Annotation => JAnnotation}
import java.lang.reflect.Field

object EnhancedClass {
  def apply(klazz:Class[_]) = new EnhancedClass(klazz)
}

class EnhancedClass(val underlying:Class[_]) {
  def hasOneOfAnnotations(annotations:Class[_ <: JAnnotation]*):Boolean = {
    annotations.find { annotation:Class[_ <: JAnnotation] =>
      underlying.isAnnotationPresent(annotation)
    }.isDefined
  }
  def findFieldsWith(annotation:Class[_ <: JAnnotation]):List[Field] = {
    underlying.getDeclaredFields.filter {
      _.isAnnotationPresent(annotation)
    }
  }
  def findFieldWith(annotation:Class[_ <: JAnnotation]):Field = {
    underlying.getDeclaredFields.filter {
      _.isAnnotationPresent(annotation)
    }.head
  }
}