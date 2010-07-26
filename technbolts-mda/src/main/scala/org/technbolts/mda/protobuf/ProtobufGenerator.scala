package org.technbolts.mda.protobuf

import java.lang.annotation.{Annotation => JAnnotation}

import org.technbolts.mda._
import domain.{DomainModel, ValueObject}
import misc.{NamedStringModel, PagingModel, ErrorModel}
import org.technbolts.reflect._
import Classes._
import _root_.java.lang.reflect.Field
import ProtobufFieldClassifier._
import org.slf4j.{LoggerFactory, Logger}
import collection.mutable.{ListBuffer, HashMap}

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

  def getProtoFileName(model:ProtobufFileModel, clazz: Class[_]): String = {
    if (useClazzAsProtoFileName)
      clazz.getCanonicalName + ".proto"
    else
      model.name + ".proto"
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

  val protobufFileModels = new HashMap[String, ProtobufFileModel]
  val protobufMessages = new HashMap[Class[_], ProtobufMessageModel]
  val plugins = new ListBuffer[ProtobufGeneratorPlugin]

  /**
   *
   */
  def generate() = {
    logger.info("Generating protobuf files")

    logger.debug("Collecting models")
    // first collect models
    getModelsWithAnnotation(classOf[ProtobufFile]).foreach {clazz => processModel(clazz)}
    logger.debug("Models collected: " + protobufFileModels.size)

    logger.debug("Collecting messages")
    // then collect message to fill models
    getModelsWithAnnotation(classOf[ProtobufMessage]).foreach {
      clazz =>
      val model = processMessage(clazz)
    }

    val count = protobufFileModels.values.foldLeft(0) {
      (prev, model) => model.messages.size + prev
    }
    logger.debug("Messages collected: " + count)

    logger.debug("Messages postScan")
    plugins.foreach { _.postScan(this) }

    // resolve dependencies if any and required import
    resolveTypes

    // make sure all ordinal are set
    protobufFileModels.values.foreach { _.messages.foreach { msg =>
        msg.generateFieldOrdinals
        msg.sortFields
      }
    }

    // then generate proto file
    logger.info("Generating #" + protobufFileModels.size + " proto file(s)")
    protobufFileModels.values.foreach { model =>

      logger.info("Generating proto file for model: " + model.name)
      val content = ProtobufTemplate.generateProtoFile(model)
      logger.info("Generated: " + model.protoFileName + "\n" + content)
    }
  }


  def resolveTypes: Unit = {
    protobufFileModels.values.foreach { fileModel =>
      fileModel.messages.foreach { message =>
        logger.debug("Resolving dependencies and field types for message: " + message.name)
        val enclosing = protobufFileModels.get(message.partOf).get
        
        message.fields.foreach { fieldModel =>
          resolveFieldType(message, fieldModel)

          if(fieldModel.classifier==ProtobufFieldClassifier.Auto) {
            logger.debug("Field classifier " + fieldModel.name + " has been set automatically to Optional")
            fieldModel.classifier = Optional
          }

          // make sure the import is updated is required
          fieldModel.fieldType match {
            case ProtobufTypeMessage(msg) =>
              val fileDependency = protobufFileModels.get(msg.partOf).get

              // prevent self dependency
              enclosing.addImport(fileDependency.protoFileName)
            case _ => //nothing special to do
          }
        }
      }
    }
  }

  private def resolveFieldType(message:ProtobufMessageModel, fieldModel:ProtobufFieldModel):Unit = {
    fieldModel.fieldType match {
      case ProtobufTypeAuto() => {
        logger.debug("Resolving type for field: " + message.name + "." + fieldModel.name)
        fieldModel.relatedField match {
          case Some(field:Field) => resolveTypeFromJField(fieldModel,field)
          case _ => throw new IllegalStateException("No java.lang.reflect.Field associated to field <"+fieldModel.name+"> to resolve type")
        }
      }
      case _ => // nothing to resolve
    }
  }

  private def resolveTypeFromJField(fieldModel:ProtobufFieldModel, f:Field):Unit = {
    val aField: ProtobufField = f.getAnnotation(classOf[ProtobufField])

    val pF:PartialFunction[ProtobufFieldType,ProtobufTypeModel] = {
      case ProtobufFieldType.Message => fieldToType(fieldModel, f)
      case ProtobufFieldType.Auto    => fieldToType(fieldModel, f)
    }
    fieldModel.fieldType = (protobufFieldTypeToType orElse pF)(aField.fieldType)
  }

  private def protobufFieldTypeToTypeUnsupported:PartialFunction[ProtobufFieldType,ProtobufTypeModel] = {
    case t:ProtobufFieldType => throw new IllegalArgumentException ("Unable to find suitable type for type "+t+".")
  }

  private def protobufFieldTypeToType:PartialFunction[ProtobufFieldType,ProtobufTypeModel] = {
      case ProtobufFieldType.Bool    => ProtobufTypeBool()
      case ProtobufFieldType.Bytes   => ProtobufTypeBytes()
      case ProtobufFieldType.Int     => ProtobufTypeInt32()
      case ProtobufFieldType.Long    => ProtobufTypeInt64()
      case ProtobufFieldType.String  => ProtobufTypeString()
      case ProtobufFieldType.Double  => ProtobufTypeDouble()
      case ProtobufFieldType.Float   => ProtobufTypeFloat()
  }

  private def fieldToCollection = (fieldModel:ProtobufFieldModel, field:Field,itemClass:Class[_]) => {
        itemClass match {
            case `byteClass` => ProtobufTypeBytes()
            case _ =>
              checkOrSetRepeated(fieldModel);
              mapClassToProtobufType(itemClass)
        }
    }

  private def fieldToType = (fieldModel:ProtobufFieldModel, field:Field) => ReflectUtils.fieldType(field) match {
    case FieldPrimitive(field) => mapClassToProtobufType(field.getType)
    case FieldRaw(field,klazz) => mapClassToProtobufType(klazz)
    case FieldArray(field,itemClass) => fieldToCollection(fieldModel, field,itemClass)
    case FieldCollection(field,itemClass) => fieldToCollection(fieldModel, field,itemClass)
    case FieldCollectionUnknownType(field)=> throw new IllegalArgumentException ("Unable to find suitable type within collection for field "+field+".")
    case FieldIterable(field,itemClass) => fieldToCollection(fieldModel, field,itemClass)
    case FieldIterableUnknownType(field)=> throw new IllegalArgumentException ("Unable to find suitable type within iterable for field "+field+".")
    case _ => throw new IllegalArgumentException ("Unable to find suitable type for field "+field+".")
  }

  private def mapClassToProtobufType(klazz:Class[_]):ProtobufTypeModel = {
    protobufMessages.get(klazz) match {
      case Some(msg) => ProtobufTypeMessage(msg)
      case _ => klazz match {
        case `stringClass`  =>  ProtobufTypeString()
        case `intClass`     =>  ProtobufTypeInt32()
        case `longClass`    =>  ProtobufTypeInt64()
        case `dateClass`    =>  ProtobufTypeInt64()
        case `floatClass`   =>  ProtobufTypeFloat()
        case `doubleClass`  =>  ProtobufTypeDouble()
        case `booleanClass` =>  ProtobufTypeBool()
        case _ => {
          // check for inlined type
          if(klazz.isAnnotationPresent(classOf[ProtobufInlined])) {
            val inlined = klazz.getAnnotation(classOf[ProtobufInlined])
            (protobufFieldTypeToType orElse protobufFieldTypeToTypeUnsupported)(inlined.fieldType)
          }
          else
            throw new IllegalArgumentException ("Type "+klazz+" is not mapped to any known Protocol Buffer type or any known message definition.")
        }
      }
    }
  }

  private def checkOrSetRepeated(fieldModel:ProtobufFieldModel):Unit = {
    fieldModel.classifier =
    fieldModel.classifier match {
      case ProtobufFieldClassifier.Auto =>
        logger.debug("Field classifier " + fieldModel.name + " has been set automatically to Repeated")
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

    protobufFileModels.get(protobufFile.name) match {
      case Some(m) => throw new IllegalStateException("Duplicate ProtobufFile model with name <" + protobufFile.name + ">")
      case _ => {
        val m = new ProtobufFileModel(protobufFile.name)
        m.protoPackage = defaultIfEmpty(protobufFile.protoPackage, getProtoPackageName(clazz))
        m.protoFileName = defaultIfEmpty(protobufFile.protoFileName, getProtoFileName(m, clazz))
        m.javaOuterClassName = defaultIfEmpty(protobufFile.javaOuterClassName, getJavaOuterClassName(clazz))
        m.javaPackage = defaultIfEmpty(protobufFile.javaPackage, getJavaPackageName(clazz))
        m.optimizedForSpeed = protobufFile.optimizedForSpeed
        protobufFileModels.put(m.name, m)
      }
    }
  }

  /**
   *
   */
  def generateModelFor(clazz: Class[_], message: ProtobufMessage): ProtobufFileModel = {
    val name = defaultIfEmpty(message.name, clazz.getSimpleName)
    logger.debug("Generating model for message: " + name)

    val m = new ProtobufFileModel(name)
    m.protoPackage = getProtoPackageName(clazz)
    m.protoFileName = getProtoFileName(m, clazz)
    m.javaOuterClassName = getJavaOuterClassName(clazz)
    m.javaPackage = getJavaPackageName(clazz)
    m.optimizedForSpeed = true
    protobufFileModels.put(m.name, m)
    m
  }

  /**
   *
   */
  def processMessage(clazz: Class[_]): Unit = {
    val message: ProtobufMessage = clazz.getAnnotation(classOf[ProtobufMessage])
    val enclosing =
      if (message.partOf.isEmpty)
        generateModelFor(clazz, message)
      else {
        protobufFileModels.get(message.partOf) match {
          case Some(e) => e
          case _ => throw new IllegalStateException("Part of <" + message.partOf + "> value does not refer to a known model")
        }
      }

    val name   = defaultIfEmpty(message.name, clazz.getSimpleName)
    val fields = getFieldsWithAnnotation(clazz, classOf[ProtobufField])
    logger.info("Processing message " + name + " in class " + clazz + " #" + fields.size + " fields found");

    val model = new ProtobufMessageModel(name, enclosing.name)
    model.relatedClass = Some(clazz);
    model.fields.appendAll(fields.map {field => processField(field)})

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

  def initDefaultPlugins:Unit = {
    plugins.append(new NameDTOFormatterPlugin)
    plugins.append(new DomainModelCRUDCommandsPlugin)
  }
}

class NameDTOFormatterPlugin extends ProtobufGeneratorPlugin {
  def postScan(generator: ProtobufGenerator):Unit = {
    generator.protobufMessages.filterKeys { isCandidate(_) }
      .values.foreach { model =>
        if(!model.name.toLowerCase.endsWith("dto"))
          model.name = model.name+"DTO"
    }
  }

  def isCandidate(klazz:Class[_]):Boolean = {
    val msg:ProtobufMessage = klazz.getAnnotation(classOf[ProtobufMessage])
    val nameNotDefined = (msg==null || msg.name.isEmpty)

    EnhancedClass(klazz).hasOneOfAnnotations(
      classOf[DomainModel],
      classOf[ValueObject]) && nameNotDefined 
  }
}

trait CommandsGenerator {
  def getGenerator:ProtobufGenerator
 
  def CMD_REQUEST_SUFFIX = "CmdRequest"
  def CMD_RESPONSE_SUFFIX = "CmdResponse"

  def formatName(value:String):String = {
    if(value.toLowerCase.endsWith("dto"))
      capitalize(value.substring(0, value.length-3))
    else
      capitalize(value);
  }

  def capitalize(value:String):String = {
    return Character.toUpperCase(value.charAt(0))+value.substring(1)
  }

  def errorModel:ProtobufMessageModel = {
    findUniqueOrNone(classOf[ErrorModel])
  }

  def pagingModel:ProtobufMessageModel = {
    findUniqueOrNone(classOf[PagingModel])
  }

  def namedStringModel:ProtobufMessageModel = {
    findUniqueOrNone(classOf[NamedStringModel])
  }

  def findUniqueOrNone(annotation:Class[_ <: JAnnotation]):ProtobufMessageModel = {
    val founds = getGenerator.protobufMessages.filterKeys { _.isAnnotationPresent(annotation)}
    if(founds.size>1)
      throw new IllegalStateException("Found multiple classes with: @"+annotation.getName)
    val (klazz,model) = founds.head
    model
  }
}

class DomainModelCRUDCommandsPlugin extends ProtobufGeneratorPlugin {
  def postScan(generator: ProtobufGenerator):Unit = {
    val filtered = generator.protobufMessages.filterKeys { isCandidate(_) }
    filtered.foreach { case (klazz,model) =>
      generateCommands(generator, klazz, model) 
    }
  }

  def isCandidate(klazz:Class[_]):Boolean = {
    klazz.isAnnotationPresent(classOf[DomainModel])
  }

  def generateCommands(generator: ProtobufGenerator, klazz:Class[_], model:ProtobufMessageModel):Unit = {
    val partOf = model.partOf
    val fileModel = generator.protobufFileModels.get(partOf).get

    val cmdGenerator = new CRUDCommandsGenerator(generator, klazz, model)

    fileModel.messages.appendAll(cmdGenerator.generateCreateCommands)
    fileModel.messages.appendAll(cmdGenerator.generateDeleteCommands)
    fileModel.messages.appendAll(cmdGenerator.generateSearchCommands)
    fileModel.messages.appendAll(cmdGenerator.generateUpdateCommands)
  }
}

class CRUDCommandsGenerator(generator: ProtobufGenerator, klazz:Class[_], model:ProtobufMessageModel) extends CommandsGenerator {
  override def getGenerator = generator
  val partOf = model.partOf

  def generateCreateCommands:Iterable[ProtobufMessageModel] = {
    val createCmd = new ProtobufMessageModel("Create"+formatName(model.name)+CMD_REQUEST_SUFFIX, model.partOf)
    val createRep = new ProtobufMessageModel("Create"+formatName(model.name)+CMD_RESPONSE_SUFFIX, partOf)

    List(createCmd, createRep)
  }

  def generateDeleteCommands:Iterable[ProtobufMessageModel] = {
    val deleteCmd = new ProtobufMessageModel("Delete"+formatName(model.name)+CMD_REQUEST_SUFFIX, partOf)
    val deleteRep = new ProtobufMessageModel("Delete"+formatName(model.name)+CMD_RESPONSE_SUFFIX, partOf)

    List(deleteCmd, deleteRep)
  }

  def generateSearchCommands:Iterable[ProtobufMessageModel] = {
    val searchCmd = new ProtobufMessageModel("Search"+formatName(model.name)+CMD_REQUEST_SUFFIX, partOf)
    searchCmd.fields.append(new ProtobufFieldModel("sample").optional.withFieldType(model))
    searchCmd.fields.append(new ProtobufFieldModel("paging").optional.withFieldType(pagingModel))
    searchCmd.fields.append(new ProtobufFieldModel("id_only").optional.withFieldType(ProtobufTypeBool()).withDefault("false"))
    searchCmd.fields.append(new ProtobufFieldModel("parameter").repeated.withFieldType(namedStringModel))

    val searchRep = new ProtobufMessageModel("Search"+formatName(model.name)+CMD_RESPONSE_SUFFIX, partOf)
    searchRep.fields.append(new ProtobufFieldModel("error").repeated.withFieldType(errorModel))
    searchRep.fields.append(new ProtobufFieldModel("found").repeated.withFieldType(model))

    List(searchCmd, searchRep)
  }

  def generateUpdateCommands:Iterable[ProtobufMessageModel] = {
    val updateCmd = new ProtobufMessageModel("Update"+formatName(model.name)+CMD_REQUEST_SUFFIX, partOf)
    val updateRep = new ProtobufMessageModel("Update"+formatName(model.name)+CMD_RESPONSE_SUFFIX, partOf)

    List(updateCmd, updateRep)
  }
}