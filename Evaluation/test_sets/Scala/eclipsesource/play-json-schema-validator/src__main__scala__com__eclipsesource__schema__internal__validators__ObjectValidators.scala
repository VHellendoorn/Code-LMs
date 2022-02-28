package com.eclipsesource.schema.internal.validators

import java.util.regex.Pattern

import com.eclipsesource.schema.internal.validation.VA
import com.eclipsesource.schema.internal.{Keywords, Props, Results, ValidationStep, ValidatorMessages}
import com.eclipsesource.schema.{SchemaProp, SchemaResolutionContext, SchemaType, SchemaValue}
import com.osinka.i18n.Lang
import play.api.libs.json._
import scalaz.{ReaderWriterState, Success}

object ObjectValidators {

  private def resultOnly(va: VA[JsValue]) = ((), (), va)

  def validatePropertyNames(propertyNames: Option[SchemaType], jsObject: JsObject)
                           (implicit lang: Lang): ValidationStep[Props] =
    ReaderWriterState { (context, status) =>
      val result: Seq[(String, VA[JsValue])] = propertyNames match {
        case None => jsObject.fields.toSeq.map(f => f._1 -> Success(f._2))
        case Some(propertyNamesSchema) =>
          jsObject.fields.foldLeft(List.empty[(String, VA[JsValue])])((validatedFields, field) =>
            validatedFields :+ field._1 -> propertyNamesSchema.validate(
              JsString(field._1),
              context.updateScope(
                _.copy(
                  schemaJsPath = context.schemaPath.map(_ \ "propertyNames"),
                  instancePath = context.instancePath \ field._1
                )
              )
            )
          )
      }
      ((), Seq.empty, Results.merge(status, Results.aggregateAsObject(result, context)))
    }

  def validateProps(properties: Seq[SchemaProp],
                    maybeRequired: Option[Seq[String]],
                    json: => JsObject
                   )(implicit lang: Lang): ValidationStep[Props] =
    ReaderWriterState { (context, status) =>

      val required = maybeRequired.getOrElse(List.empty[String])

      val validated = properties.foldLeft(List.empty[(String, VA[JsValue])])((props, attr) =>
        json \ attr.name match {
          case _: JsUndefined => if (required.contains(attr.name)) {
            attr.name ->
              Results.failureWithPath(
                Keywords.Object.Required,
                ValidatorMessages("obj.required.prop", attr.name),
                context,
                json
              ) :: props
          } else {
            props
          }
          case JsDefined(value) =>
            (attr.name ->
              attr.schemaType.validate(
                value,
                context.updateScope(
                  _.copy(
                    schemaJsPath = context.schemaPath.map(_ \ "properties" \ attr.name),
                    instancePath = context.instancePath \ attr.name
                  )
                )
              )) :: props
        }
      )

      val missing = required.filterNot(req => validated.exists(_._1 == req))
        .foldLeft(List.empty[(String, VA[JsValue])]) { (acc, req) =>
          json \ req match {
            case _: JsUndefined =>
              val result = req ->
                Results.failureWithPath(
                  Keywords.Object.Required,
                  ValidatorMessages("obj.required.prop", req),
                  context,
                  json
                )
              result :: acc
            case _ => acc
          }
        }

      val result = validated ++ missing

      val validatedProperties = result.map(_._1)
      val unvalidatedProps: Props = json.fields.toSeq.filterNot(field => validatedProperties.contains(field._1))

      ((), unvalidatedProps, Results.merge(status, Results.aggregateAsObject(result, context)))
    }

  def validatePatternProps(patternProps: Option[Map[String, SchemaType]], props: Props)
                          (implicit lang: Lang): ValidationStep[Props] =
    ReaderWriterState { (context, status) =>

      // find all matching properties and validate them
      val validated: Seq[(String, VA[JsValue])] = props.flatMap {
        prop => {
          val matchedPatternProperties: Iterable[(String, SchemaType)] = patternProps.getOrElse(Seq.empty).filter(pp => {
            val pattern = Pattern.compile(pp._1)
            val matcher = pattern.matcher(prop._1)
            matcher.find()
          })
          matchedPatternProperties.map(pp => {
            prop._1 -> pp._2.validate(prop._2, context.updateScope(
              _.copy(
                schemaJsPath = context.schemaPath.map(_ \ "properties" \ prop._1),
                instancePath = context.instancePath \ prop._1
              )
            ))
          }
          )
        }
      }

      val validatedProperties = validated.map(_._1)
      val unmatchedProps = props.filterNot(prop =>
        validatedProperties.contains(prop._1)
      )

      ((), unmatchedProps, Results.merge(status, Results.aggregateAsObject(validated, context)))
    }

  def validateAdditionalProps(additionalProps: Option[SchemaType], unmatchedFields: Props, json: JsValue)
                             (implicit lang: Lang): ValidationStep[Unit] = {

    def validateUnmatched(schemaType: SchemaType, context: SchemaResolutionContext): VA[JsValue] = {
      val validated = unmatchedFields.map { attr =>
        attr._1 -> schemaType.validate(
          attr._2,
          context.updateScope(
            _.copy(
              schemaJsPath = context.schemaPath.map(_ \ Keywords.Object.AdditionalProperties),
              instancePath = context.instancePath \ attr._1
            )
          )
        )
      }
      Results.aggregateAsObject(validated, context)
    }

    ReaderWriterState { (context, status) =>

      if (unmatchedFields.isEmpty) {
        resultOnly(status)
      } else {
        additionalProps match {
          case Some(SchemaValue(JsBoolean(enabled))) =>
            if (enabled) resultOnly(Results.merge(status, Success(JsObject(unmatchedFields))))
            else resultOnly(
              Results.merge(status,
                Results.failureWithPath(
                  Keywords.Object.AdditionalProperties,
                  ValidatorMessages("obj.additional.props", unmatchedFields.map { case (name, _) => s"'$name'" }.mkString(" and ")),
                  context,
                  json
                )
              ))
          case Some(additionalProp) =>
            val validationStatus = validateUnmatched(additionalProp, context)
            resultOnly(Results.merge(status, validationStatus))
          // shouldn't happen
          case _ => resultOnly(status)
        }
      }
    }
  }

  def validateDependencies(schema: SchemaType, deps: Option[Map[String, SchemaType]], json: JsObject)
                          (implicit lang: Lang): ValidationStep[Unit] = {

    def validatePropertyDependency(propName: String, dependencies: Seq[String], context: SchemaResolutionContext): VA[JsValue] = {

      // check if property is present at all
      val mandatoryProps = json.fields
        .find(_._1 == propName)
        .map(_ => dependencies)
        .getOrElse(Seq.empty[String])

      // if present, make sure all dependencies are fulfilled
      val result = mandatoryProps.map(prop => json.fields.find(_._1 == prop).fold(
        prop -> Results.failureWithPath(
          Keywords.Object.Dependencies,
          ValidatorMessages("obj.missing.prop.dep", prop),
          context.updateScope(_.copy(
            schemaJsPath = context.schemaPath.map(_ \ prop),
            instancePath = context.instancePath \ prop
          )),
          json
        )
      )(field => Results.success(field)))

      Results.aggregateAsObject(result, context)
    }

    ReaderWriterState { (context, status) =>

      val dependencies = deps.getOrElse(Seq.empty)
      val updatedStatus = dependencies.foldLeft(status) { case (currStatus, dep) =>
        dep match {
          case (name, SchemaValue(JsArray(values))) =>
            // collecting strings should not be necessary at this point
            val validated = validatePropertyDependency(name, values.toSeq.collect { case JsString(str) => str }, context)
            Results.merge(currStatus, validated)
          case (name, dep: SchemaType) if json.keys.contains(name) =>
            val validated = dep.validate(json, context)
            Results.merge(currStatus, validated)
          case _ => currStatus
        }
      }

      ((), (), updatedStatus)
    }
  }

  def validateMaxProperties(maxProperties: Option[Int], json: JsObject)
                           (implicit lang: Lang): ReaderWriterState[SchemaResolutionContext, Unit, VA[JsValue], Unit] = {
    ReaderWriterState { (context, status) =>
      val size = json.fields.size
      val result = maxProperties match {
        case None => Success(json)
        case Some(max) =>
          if (size <= max)  Success(json)
          else  Results.failureWithPath(
            Keywords.Object.MaxProperties,
            ValidatorMessages("obj.max.props", size, max),
            context,
            json
          )
      }
      ((), (), Results.merge(status, result))
    }
  }

  def validateMinProperties(minProperties: Option[Int], json: JsObject)
                           (implicit lang: Lang): ReaderWriterState[SchemaResolutionContext, Unit, VA[JsValue], Unit] = {
    ReaderWriterState { (context, status) =>
      val size = json.fields.size
      val result= minProperties match {
        case None => Success(json)
        case Some(min) => if (size >= min) {
          Success(json)
        } else {
          Results.failureWithPath(
            Keywords.Object.MinProperties,
            ValidatorMessages("obj.min.props", size, min),
            context,
            json
          )
        }
      }
      ((), (), Results.merge(status, result))
    }
  }

}
