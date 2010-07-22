package org.technbolts.mda

import org.junit.Test
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import collection.mutable.ListBuffer
import collection.JavaConversions._
import org.springframework.util.ClassUtils
import org.springframework.core.`type`.filter.{AnnotationTypeFilter, AssignableTypeFilter}
import protobuf.ProtobufMessage

class ClassScanTest {

  @Test
  def useCase():Unit = {
    val scanner = new ComponentClassScanner
    //scanner.addIncludeFilter(new AssignableTypeFilter())
    scanner.addIncludeFilter(new AnnotationTypeFilter(classOf[ProtobufMessage]))
    scanner.getComponentClasses("org.technbolts").foreach {
      klazz => println(klazz)
    }
  }
}


class ComponentClassScanner extends ClassPathScanningCandidateComponentProvider(false) {

	def getComponentClasses(basePackage:String):List[Class[_]] = {
		val basePkg = if (basePackage == null) "" else basePackage

		val classes = new ListBuffer[Class[_]]
		for (candidate <- findCandidateComponents(basePkg)) {
      val cls:Class[_] = ClassUtils.resolveClassName(
                          candidate.getBeanClassName(),
                          ClassUtils.getDefaultClassLoader());
      classes.add(cls);
		}
		classes.toList
	}

}
