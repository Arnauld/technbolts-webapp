package org.technbolts.mda

import java.lang.{Class => JClass}

import org.slf4j.{LoggerFactory, Logger}
import java.lang.reflect.{Type, ParameterizedType, Field => JField}

sealed abstract class FieldType {
  def field: JField
}
case class FieldEnum(field: JField) extends FieldType
case class FieldPrimitive(field: JField) extends FieldType
case class FieldArray(field: JField, itemClass: Class[_]) extends FieldType
case class FieldCollection(field: JField, itemClass: Class[_]) extends FieldType
case class FieldCollectionUnknownType(field: JField) extends FieldType
case class FieldIterable(field: JField, itemClass: Class[_]) extends FieldType
case class FieldIterableUnknownType(field: JField) extends FieldType
case class FieldRaw(field: JField, itemClass: Class[_]) extends FieldType

class ReflectUtils
object ReflectUtils {
  val logger: Logger = LoggerFactory.getLogger(classOf[ReflectUtils])

  def fieldType(field: JField): FieldType = {
    logger.debug("Examining field <{}>: {}", field.getName, field)

    val fieldType = field.getType

    // special cases
    if (fieldType.isEnum) {
      logger.debug("Field <{}> considered as an enum of {}", field.getName, fieldType.getEnumConstants)
      FieldEnum(field)
    }
    else if (fieldType.isPrimitive) {
      logger.debug("Field <{}> considered as a primitive {}", field.getName, fieldType)
      FieldPrimitive(field)
    }
    else if (fieldType.isArray) {
      val itemClass = field.getType.getComponentType
      logger.debug("Field <{}> considered as Array of {}", field.getName, itemClass)
      FieldArray(field, itemClass)
    }
    else if (classOf[java.util.Collection[_]].isAssignableFrom(field.getType)) {
      lookupItemType(field) match {
        case Some(itemClass: JClass[_]) =>
          logger.debug("Field <{}> considered as <java/Collection> of {}", field.getName, itemClass)
          FieldCollection(field, itemClass)
        case _ =>
          logger.warn("Field <{}> considered as <java/Collection> but no type could be retrieved, field will not be persisted. Field: {}", field.getName, field)
          FieldCollectionUnknownType(field)
      }
    }
    else if (classOf[scala.Iterable[_]].isAssignableFrom(fieldType)) {
      lookupItemType(field) match {
        case Some(itemClass: JClass[_]) =>
          logger.debug("Field <{}> considered as <scala/Iterable> of {}", field.getName, itemClass)
          FieldIterable(field, itemClass)
        case _ =>
          logger.warn("Field <{}> considered as <scala/Iterable> but no type could be retrieved. Field: {}", field.getName, field)
          FieldIterableUnknownType(field)
      }
    }
    else {
      logger.debug("Field <{}> considered as direct value of {}", field.getName, fieldType)
      FieldRaw(field, fieldType)
    }
  }

  def lookupItemType(field: JField): Option[JClass[_]] = field.getGenericType() match {
    case t: ParameterizedType =>
      val typeArg = t.getActualTypeArguments()(0)
      Some(getJClass(typeArg))
    case _ => None
  }

  def getJClass(typeArg: Type): JClass[_] = {
    typeArg match {
      case p: ParameterizedType => p.getRawType.asInstanceOf[JClass[_]]
      case k: JClass[_] => k
      case _ => null
    }
  }
}