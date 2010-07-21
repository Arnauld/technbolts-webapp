package org.technbolts.mda.protobuf

import org.technbolts.mda._
import Classes._
import collection.mutable.HashMap
import _root_.java.lang.reflect.Field
import ProtobufFieldClassifier._
import ProtobufFieldType._
import org.slf4j.{LoggerFactory, Logger}


class ProtobufGenerator extends Generator {
  private val logger: Logger = LoggerFactory.getLogger(classOf[ProtobufGenerator])

  //
  var useClazzAsProtoPackage = false
  var protobufSubPackage: String = "protobuf"
  var protobufPackage: String = _
  //
  var useClazzAsProtoFileName = false
  //
  var useClazzAsJavaPackage = false
  var javaSubPackage: String = "protobuf"
  var javaPackage: String = _
  //
  var useClazzAsJavaOuterClassName = false
  //
  var fieldFormatter = (s: String) => {s.replaceAll("[A-Z]", "_$0").toLowerCase}

  def getProtoPackageName(clazz: Class[_]): String = {
    if (useClazzAsProtoPackage)
      clazz.getPackage.getName
    else {
      if (protobufPackage == null)
        protobufPackage = generatorModel.rootPackage + "." + protobufSubPackage
      protobufPackage
    }
  }

  def getProtoFileName(clazz: Class[_]): String = {
    if (useClazzAsProtoFileName)
      clazz.getCanonicalName + ".proto"
    else
    // what else to return ?
      clazz.getCanonicalName + ".proto"
  }

  def getJavaPackageName(clazz: Class[_]): String = {
    if (useClazzAsJavaPackage)
      clazz.getPackage.getName
    else {
      if (javaPackage == null)
        javaPackage = generatorModel.rootPackage + "." + javaSubPackage
      javaPackage
    }
  }

  def getJavaOuterClassName(clazz: Class[_]): String = {
    if (useClazzAsJavaOuterClassName)
      clazz.getSimpleName
    else
    // what else to return ?
      clazz.getSimpleName
  }

  val protobufModels = new HashMap[String, ProtobufModel]
  val protobufMessages = new HashMap[Class[_], ProtobufMessageModel]

  /**
   *
   */
  def generate() = {
    logger.info("Generating protobuf files")

    logger.info(">Collecting models")
    // first collect models
    getModelsWithAnnotation(classOf[Protobuf]).foreach {clazz => processModel(clazz)}
    logger.info(">Models collected: " + protobufModels.size)

    logger.info(">Collecting messages")
    // then collect message to fill models
    getModelsWithAnnotation(classOf[ProtobufMessage]).foreach {
      clazz =>
      val model = processMessage(clazz)
    }

    val count = protobufModels.values.foldLeft(0) {
      (prev, model) => model.messages.size + prev
    }
    logger.info(">Messages collected: " + count)

    // resolve dependencies if any and required import
    resolveDependencies
    
    // then generate proto file
    logger.info(">Generating #" + protobufModels.size + " proto file(s)")
    protobufModels.values.foreach {
      model =>
        logger.info(">Generating proto file for model: " + model.name)
        val content = ProtobufTemplate.generateProtoFile(model)
        logger.debug(">Generated: " + model.protoFileName + "\n" + content)
    }
  }

  def resolveDependencies: Unit = {
    protobufMessages.values.foreach {
      message =>
        logger.info(">Resolving dependencies and field types for message: " + message.name)
        message.fields.foreach { fieldModel =>
          fieldModel.fieldType match {
            case ProtobufFieldType.Auto => resolveFieldType(message, fieldModel)
            case _ => //nothing special to do
          }
          if(fieldModel.classifier==ProtobufFieldClassifier.Auto) {
            logger.info("Field classifier " + fieldModel.name + " has been set automatically to Optional")
            fieldModel.classifier = Optional
          }
        }
    }
  }

  private def resolveFieldType(message:ProtobufMessageModel, fieldModel:ProtobufFieldModel):Unit = {
    logger.info(">Resolving type for field: " + message.name + "." + fieldModel.name)
    fieldModel.relatedField match {
      case Some(field:Field) => resolveTypeFromJField(fieldModel,field)
      case _ => throw new IllegalStateException("No java.lang.reflect.Field associated to field <"+fieldModel.name+"> to resolve type")
    }
  }

  private def resolveTypeFromJField(fieldModel:ProtobufFieldModel, f:Field):Unit = {
    ReflectUtils.fieldType(f) match {
      case FieldPrimitive(field) => fieldModel.fieldType = protobufTypeMapping(field.getType)
      case FieldRaw(field,klazz) => fieldModel.fieldType = protobufTypeMapping(klazz)
      case FieldArray(field,itemClass) =>
        itemClass match {
          case `byteClass` => fieldModel.fieldType = ProtobufTypeBytes()
          case _ =>
            checkOrSetRepeated(fieldModel);
            fieldModel.fieldType = protobufTypeMapping(itemClass)
        }
    }
  }

  private def protobufTypeMapping(klazz:Class[_]):ProtobufType = {
    protobufMessages.get(klazz) match {
      case Some(msg) => ProtobufFieldMessage(msg.name)
      case _ => klazz match {
        case `stringClass` =>  ProtobufFieldString()
        case `intClass`    =>  ProtobufFieldInt()
        case `longClass`   =>  ProtobufFieldLong()
        case `dateClass`   =>  ProtobufFieldLong()
        case `floatClass`  =>  ProtobufFieldFloat()
        case `doubleClass` =>  ProtobufFieldDouble()
        case _ => throw new IllegalArgumentException ("Type "+klazz+" is not mapped to any known Protocol Buffer type.")
      }
    }
  }

  private def checkOrSetRepeated(fieldModel:ProtobufFieldModel):Unit = {
    fieldModel.classifier =
    fieldModel.classifier match {
      case ProtobufFieldClassifier.Auto =>
        logger.info("Field classifier " + fieldModel.name + " has been set automatically to Repeated")
        Repeated
      case Repeated =>
        Repeated
      case c:ProtobufFieldClassifier =>
        logger.warn("Field classifier " + fieldModel.name + " is not appropriate, you should probably classify it as Auto or Repeated")
        c
    }
  }

  /**
   *
   */
  def processModel(clazz: Class[_]): Unit = {
    val protobuf: Protobuf = clazz.getAnnotation(classOf[Protobuf])

    protobufModels.get(protobuf.name) match {
      case Some(m) => throw new IllegalStateException("Duplicate Protobuf model with name <" + protobuf.name + ">")
      case _ => {
        val m = new ProtobufModel(protobuf.name)
        m.protoPackage = defaultIfEmpty(protobuf.protoPackage, getProtoPackageName(clazz))
        m.protoFileName = defaultIfEmpty(protobuf.protoFileName, getProtoFileName(clazz))
        m.javaOuterClassName = defaultIfEmpty(protobuf.javaOuterClassName, getJavaOuterClassName(clazz))
        m.javaPackage = defaultIfEmpty(protobuf.javaPackage, getJavaPackageName(clazz))
        m.optimizeForSpeed = protobuf.optimizeForSpeed
        protobufModels.put(m.name, m)
      }
    }
  }

  /**
   *
   */
  def generateModelFor(clazz: Class[_], message: ProtobufMessage): ProtobufModel = {
    logger.info(">Generating model for message: " + message.message)

    val m = new ProtobufModel(message.message)
    m.protoPackage = getProtoPackageName(clazz)
    m.protoFileName = getProtoFileName(clazz)
    m.javaOuterClassName = getJavaOuterClassName(clazz)
    m.javaPackage = getJavaPackageName(clazz)
    m.optimizeForSpeed = true
    protobufModels.put(m.name, m)
    m
  }

  /**
   *
   */
  def processMessage(clazz: Class[_]): Unit = {
    val message: ProtobufMessage = clazz.getAnnotation(classOf[ProtobufMessage])
    val model = new ProtobufMessageModel(message.message, message.partOf)

    val fields = getFieldsWithAnnotation(clazz, classOf[ProtobufField])
    logger.info("Processing message #" + fields.size + " fields found");

    model.relatedClass = Some(clazz);
    model.fields.appendAll(fields.map {field => processField(field)})
    model.generateFieldOrdinals
    model.sortFields

    val enclosing =
    if (message.partOf.isEmpty)
      generateModelFor(clazz, message)
    else {
      protobufModels.get(message.partOf) match {
        case Some(e) => e
        case _ => throw new IllegalStateException("Part of <" + message.partOf + "> value does not refer to a known model")
      }
    }
    enclosing.messages.append(model)

    protobufMessages.put(clazz, model)
  }

  /**
   *
   */
  def getFieldName(field: Field) = fieldFormatter(field.getName)

  /**
   *
   */
  def processField(field: Field): ProtobufFieldModel = {
    val aField: ProtobufField = field.getAnnotation(classOf[ProtobufField])

    val model = new ProtobufFieldModel(defaultIfEmpty(aField.name, getFieldName(field)))
    model.ordinal = if (aField.order > 0) Some(aField.order) else None
    model.classifier = aField.classifier
    model.relatedField = Some(field);
    model.fieldType = aField.fieldType
    model
  }
}