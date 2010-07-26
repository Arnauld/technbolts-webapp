package org.technbolts.reflect

import java.lang.annotation.{Annotation => JAnnotation}

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import collection.mutable.ListBuffer
import collection.JavaConversions._
import org.springframework.util.ClassUtils
import ClassUtils._
import org.springframework.core.`type`.classreading.{MetadataReader, MetadataReaderFactory}
import org.springframework.core.`type`.filter.{AnnotationTypeFilter, TypeFilter}
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition

object ClassScanner {
  def composeFiltersOr(filters:Iterable[TypeFilter]):TypeFilter = {
    new TypeFilter {
      def `match`(reader:MetadataReader, factory:MetadataReaderFactory):Boolean = {
        filters.find { _.`match`(reader,factory) }.isDefined
      }
    }
  }

  def composeAnnotationsOr(annotations:Class[_ <: JAnnotation]*):TypeFilter = {
    composeFiltersOr(annotations.map {new AnnotationTypeFilter(_)})
  }
}

class ClassScanner extends ClassPathScanningCandidateComponentProvider(false) {

  var concreteOnly = false

  def getComponentClasses(basePackage: String): List[Class[_]] = {
    val basePkg = if (basePackage == null) "" else basePackage

    val classes = new ListBuffer[Class[_]]
    for (candidate <- findCandidateComponents(basePkg)) {
      val klazzName = candidate.getBeanClassName()
      val klazz: Class[_] = resolveClassName(klazzName, getDefaultClassLoader());
      classes.add(klazz);
    }
    classes.toList
  }


  override def isCandidateComponent(beanDefinition: AnnotatedBeanDefinition) = {
    if(concreteOnly)
      super.isCandidateComponent(beanDefinition)
    else
      true
  }
}