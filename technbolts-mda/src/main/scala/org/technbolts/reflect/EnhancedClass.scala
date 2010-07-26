package org.technbolts.reflect

import java.lang.annotation.{Annotation => JAnnotation}

object EnhancedClass {
  def apply(klazz:Class[_]) = new EnhancedClass(klazz)
}

class EnhancedClass(val underlying:Class[_]) {
  def hasOneOfAnnotations(annotations:Class[_ <: JAnnotation]*):Boolean = {
    annotations.find { annotation:Class[_ <: JAnnotation] =>
      underlying.isAnnotationPresent(annotation)
    }.isDefined
  }
}