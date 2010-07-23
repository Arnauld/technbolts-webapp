package org.technbolts.reflect

import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import collection.mutable.ListBuffer
import collection.JavaConversions._
import org.springframework.util.ClassUtils
import ClassUtils._

class ComponentClassScanner extends ClassPathScanningCandidateComponentProvider(false) {
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

}