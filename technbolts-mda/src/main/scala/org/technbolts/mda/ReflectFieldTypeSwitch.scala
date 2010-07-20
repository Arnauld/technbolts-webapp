package org.technbolts.mda

import java.lang.{Class => JClass}

import org.slf4j.{LoggerFactory, Logger}
import java.lang.reflect.{Type, ParameterizedType, Field => JField}

class ReflectFieldTypeSwitch
trait ReflectFieldTypeSwitch {

  def caseFieldEnum(field:JField):Unit = {}
  def caseFieldPrimitive(field:JField):Unit = {}
  def caseFieldArray(field:JField,itemClass:Class[_]):Unit = {}
  def caseFieldCollection(field:JField,itemClass:Class[_]):Unit = {}
  def caseFieldCollectionUnknownType(field:JField,itemClass:Class[_]):Unit = {}
  def caseFieldIterable(field:JField,itemClass:Class[_]):Unit = {}
  def caseFieldIterableUnknownType(field:JField,itemClass:Class[_]):Unit = {}
  def caseFieldRaw(field:JField,itemClass:Class[_]):Unit = {}

  val logger:Logger = LoggerFactory.getLogger(classOf[ReflectFieldTypeSwitch])

  def fieldDescriptor(field:JField):Unit = {
    logger.debug("Examining field <{}>: {}", field.getName, field)

    val fieldType = field.getType

    // special cases
    if(fieldType.isEnum) {
      logger.debug("Field <{}> considered as an enum of {}", field.getName, fieldType.getEnumConstants)
      caseFieldEnum(field)
    }
    else if(fieldType.isPrimitive) {
      logger.debug("Field <{}> considered as a primitive {}", field.getName, fieldType)
      caseFieldPrimitive(field)
    }
    else if (fieldType.isArray) {
      val itemClass = field.getType.getComponentType
      logger.debug("Field <{}> considered as Array of {}", field.getName, itemClass)
      caseFieldArray(field, itemClass)
    }
    else if (classOf[java.util.Collection[_]].isAssignableFrom(field.getType)) {
      lookupItemType(field) match {
        case Some(itemClass: JClass[_]) =>
          logger.debug("Field <{}> considered as <java/Collection> of {}", field.getName, itemClass)
          caseFieldCollection(field, itemClass)
        case None =>
          logger.warn("Field <{}> considered as <java/Collection> but no type could be retrieved, field will not be persisted. Field: {}", field.getName, field)
          caseFieldCollectionUnknownType(field, itemClass)
      }
    }
    else if (classOf[scala.Iterable[_]].isAssignableFrom(fieldType)) {
      lookupItemType(field) match {
        case Some(itemClass: JClass[_]) =>
          logger.debug("Field <{}> considered as <scala/Iterable> of {}", field.getName, itemClass)
          caseFieldIterable(field, itemClass)
        case None =>
          logger.warn("Field <{}> considered as <scala/Iterable> but no type could be retrieved. Field: {}", field.getName, field)
          caseFieldIterableUnknownType(field, itemClass)
      }
    }
    else {
      logger.debug("Field <{}> considered as direct value of {}", field.getName, fieldType)
      caseFieldRaw(field, fieldType)
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