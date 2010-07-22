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

  val protobufModels = new HashMap[String, ProtobufFileModel]
  val protobufMessages = new HashMap[Class[_], ProtobufMessageModel]

  /**
   *
   */
  def generate() = {
    logger.info("Generating protobuf files")

    logger.info(">Collecting models")
    // first collect models
    getModelsWithAnnotation(classOf[ProtobufFile]).foreach {clazz => processModel(clazz)}
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
    resolveTypes
    
    // then generate proto file
    logger.info(">Generating #" + protobufModels.size + " proto file(s)")
    protobufModels.values.foreach {
      model =>
        logger.info(">Generating proto file for model: " + model.name)
        val content = ProtobufTemplate.generateProtoFile(model)
        logger.debug(">Generated: " + model.protoFileName + "\n" + content)
    }
  }

  def resolveTypes: Unit = {
    protobufMessages.values.foreach {
      message =>
        logger.info(">Resolving dependencies and field types for message: " + message.name)
        message.fields.foreach { fieldModel =>
          resolveFieldType(message, fieldModel)

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
    val aField: ProtobufField = f.getAnnotation(classOf[ProtobufField])

    val fieldToCollection = (field:Field,itemClass:Class[_]) => {
        itemClass match {
            case `byteClass` => ProtobufTypeBytes()
            case _ =>
              checkOrSetRepeated(fieldModel);
              mapClassToProtobufType(itemClass)
        }
    }

    val fieldToType = (field:Field) => ReflectUtils.fieldType(field) match {
        case FieldPrimitive(field) => mapClassToProtobufType(field.getType)
        case FieldRaw(field,klazz) => mapClassToProtobufType(klazz)
        case FieldArray(field,itemClass) => fieldToCollection(field,itemClass)
        case FieldCollection(field,itemClass) => fieldToCollection(field,itemClass)
        case FieldCollectionUnknownType(field)=> throw new IllegalArgumentException ("Unable to find suitable type within collection for field "+field+".")
        case FieldIterable(field,itemClass) => fieldToCollection(field,itemClass)
        case FieldIterableUnknownType(field)=> throw new IllegalArgumentException ("Unable to find suitable type within iterable for field "+field+".")
        case _ => throw new IllegalArgumentException ("Unable to find suitable type for field "+field+".")
      }

    fieldModel.fieldType = aField.fieldType match {
      case ProtobufFieldType.Bool    => ProtobufTypeBool()
      case ProtobufFieldType.Bytes   => ProtobufTypeBytes()
      case ProtobufFieldType.Int     => ProtobufTypeInt32()
      case ProtobufFieldType.Long    => ProtobufTypeInt64()
      case ProtobufFieldType.String  => ProtobufTypeString()
      case ProtobufFieldType.Double  => ProtobufTypeDouble()
      case ProtobufFieldType.Float   => ProtobufTypeFloat()
      case ProtobufFieldType.Message => fieldToType(f)
      case ProtobufFieldType.Auto    => fieldToType(f)
    }

  }

  private def mapClassToProtobufType(klazz:Class[_]):ProtobufTypeModel = {
    protobufMessages.get(klazz) match {
      case Some(msg) => ProtobufTypeMessage(msg.name)
      case _ => klazz match {
        case `stringClass`  =>  ProtobufTypeString()
        case `intClass`     =>  ProtobufTypeInt32()
        case `longClass`    =>  ProtobufTypeInt64()
        case `dateClass`    =>  ProtobufTypeInt64()
        case `floatClass`   =>  ProtobufTypeFloat()
        case `doubleClass`  =>  ProtobufTypeDouble()
        case `booleanClass` =>  ProtobufTypeBool()
        case _ => throw new IllegalArgumentException ("Type "+klazz+" is not mapped to any known Protocol Buffer type or any known message definition.")
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
    val protobufFile: ProtobufFile = clazz.getAnnotation(classOf[ProtobufFile])

    protobufModels.get(protobufFile.name) match {
      case Some(m) => throw new IllegalStateException("Duplicate ProtobufFile model with name <" + protobufFile.name + ">")
      case _ => {
        val m = new ProtobufFileModel(protobufFile.name)
        m.protoPackage = defaultIfEmpty(protobufFile.protoPackage, getProtoPackageName(clazz))
        m.protoFileName = defaultIfEmpty(protobufFile.protoFileName, getProtoFileName(clazz))
        m.javaOuterClassName = defaultIfEmpty(protobufFile.javaOuterClassName, getJavaOuterClassName(clazz))
        m.javaPackage = defaultIfEmpty(protobufFile.javaPackage, getJavaPackageName(clazz))
        m.optimizedForSpeed = protobufFile.optimizedForSpeed
        protobufModels.put(m.name, m)
      }
    }
  }

  /**
   *
   */
  def generateModelFor(clazz: Class[_], message: ProtobufMessage): ProtobufFileModel = {
    val name = defaultIfEmpty(message.name, clazz.getSimpleName)
    logger.info(">Generating model for message: " + name)

    val m = new ProtobufFileModel(name)
    m.protoPackage = getProtoPackageName(clazz)
    m.protoFileName = getProtoFileName(clazz)
    m.javaOuterClassName = getJavaOuterClassName(clazz)
    m.javaPackage = getJavaPackageName(clazz)
    m.optimizedForSpeed = true
    protobufModels.put(m.name, m)
    m
  }

  /**
   *
   */
  def processMessage(clazz: Class[_]): Unit = {
    val message: ProtobufMessage = clazz.getAnnotation(classOf[ProtobufMessage])

    val model = new ProtobufMessageModel(defaultIfEmpty(message.name, clazz.getSimpleName), message.partOf)

    val fields = getFieldsWithAnnotation(clazz, classOf[ProtobufField])
    logger.info("Processing message " + model.name + " #" + fields.size + " fields found");

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
    model
  }
}