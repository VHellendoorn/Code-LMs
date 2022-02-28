package com.eclipsesource.schema.internal.draft4.constraints

import com.eclipsesource.schema.{SchemaMap, SchemaProp, SchemaResolutionContext, SchemaSeq, SchemaType, SchemaValue}
import com.eclipsesource.schema.internal.Keywords
import com.eclipsesource.schema.internal.constraints.Constraints.AnyConstraints
import com.eclipsesource.schema.internal.validation.{Rule, VA}
import com.osinka.i18n.Lang
import play.api.libs.json.{JsArray, JsString, JsValue}
import scalaz.std.option._
import scalaz.std.set._
import scalaz.syntax.semigroup._

case class AnyConstraints4(schemaType: Option[String] = None,
                           allOf: Option[Seq[SchemaType]] = None,
                           anyOf: Option[Seq[SchemaType]] = None,
                           oneOf: Option[Seq[SchemaType]] = None,
                           definitions: Option[Map[String, SchemaType]] = None,
                           enum: Option[Seq[JsValue]] = None,
                           not: Option[SchemaType] = None,
                           description: Option[String] = None,
                           id: Option[String] = None
                          )
  extends AnyConstraints {

  override def subSchemas: Set[SchemaType] =
    (definitions.map(_.values.toSet) |+| allOf.map(_.toSet) |+| anyOf.map(_.toSet) |+| oneOf.map(_.toSet))
      .getOrElse(Set.empty[SchemaType])

  override def resolvePath(path: String): Option[SchemaType] = path match {
    case Keywords.Any.Type => schemaType.map(t => SchemaValue(JsString(t)))
    case Keywords.Any.AllOf => allOf.map(types => SchemaSeq(types))
    case Keywords.Any.AnyOf => anyOf.map(types => SchemaSeq(types))
    case Keywords.Any.OneOf => oneOf.map(types => SchemaSeq(types))
    case Keywords.Any.Definitions => definitions.map(entries =>
      SchemaMap(
        Keywords.Any.Definitions,
        entries.toSeq.map { case (name, schema) => SchemaProp(name, schema) })
    )
    case Keywords.Any.Enum => enum.map(e => SchemaValue(JsArray(e)))
    case Keywords.Any.Not => not
    case "id" => id.map(id => SchemaValue(JsString(id)))
    case _ => None
  }

  import com.eclipsesource.schema.internal.validators.AnyConstraintValidators._

  override def validate(schema: SchemaType, json: JsValue, context: SchemaResolutionContext)(implicit lang: Lang): VA[JsValue] = {
    val reader: scalaz.Reader[SchemaResolutionContext, Rule[JsValue, JsValue]] = for {
      allOfRule <- validateAllOf(schema, allOf)
      anyOfRule <- validateAnyOf(schema, anyOf)
      oneOfRule <- validateOneOf(schema, oneOf)
      enumRule <- validateEnum(enum)
      notRule <- validateNot(not)
    } yield allOfRule |+| anyOfRule |+| oneOfRule |+| enumRule |+| notRule
    reader
      .run(context)
      .repath(_.compose(context.instancePath))
      .validate(json)
  }

}
