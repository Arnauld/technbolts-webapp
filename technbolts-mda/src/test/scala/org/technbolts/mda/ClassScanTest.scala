package org.technbolts.mda

import org.junit.Test
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import collection.mutable.ListBuffer
import collection.JavaConversions._
import org.springframework.util.ClassUtils
import ClassUtils._
import org.springframework.core.`type`.filter.{AnnotationTypeFilter}
import protobuf.ProtobufMessage
import org.technbolts.reflect.ClassScanner

class ClassScanTest {
  @Test
  def useCase(): Unit = {
    val scanner = new ClassScanner
    //scanner.addIncludeFilter(new AssignableTypeFilter())
    scanner.addIncludeFilter(new AnnotationTypeFilter(classOf[ProtobufMessage]))
    scanner.getComponentClasses("org.technbolts").foreach {
      klazz => println(klazz)
    }
  }
}


