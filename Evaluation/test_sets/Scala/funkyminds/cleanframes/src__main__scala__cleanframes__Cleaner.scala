package cleanframes

import org.apache.spark.sql.{Column, DataFrame, functions}
import shapeless.labelled.FieldType
import shapeless.{::, HList, HNil, LabelledGeneric, Lazy, Witness}

trait Cleaner[A] {
  def clean(frame: DataFrame, name: Option[String], alias: Option[String]): List[Column]
}

object Cleaner {
  def apply[A](frame: DataFrame, name: Option[String], alias: Option[String])(implicit env: Cleaner[A]): DataFrame = {
    frame.select(
      env.clean(frame, name, alias): _*
    )
  }

  def materialize[A](func: (DataFrame, Option[String], Option[String]) => List[Column]): Cleaner[A] = new Cleaner[A] {
    override def clean(frame: DataFrame, name: Option[String], alias: Option[String]): List[Column] = func(frame, name, alias)
  }

  implicit val hnilCleaner: Cleaner[HNil] = materialize((_, _, _) => Nil)

  implicit def genericObjectCleaner[A, H <: HList](implicit
                                                   gen: LabelledGeneric.Aux[A, H],
                                                   hCleaner: Lazy[Cleaner[H]]): Cleaner[A] =
    materialize((frame, name, alias) => {
      val structColumn = functions.struct(
        hCleaner.value.clean(frame, name, alias): _*
      )

      List(
        alias
          .map(structColumn.as)
          .getOrElse(structColumn)
      )
    })

  implicit def hlistObjectCleaner[K <: Symbol, H, T <: HList](implicit
                                                              witness: Witness.Aux[K],
                                                              hCleaner: Lazy[Cleaner[H]],
                                                              tCleaner: Cleaner[T]): Cleaner[FieldType[K, H] :: T] = {
    val fieldName: String = witness.value.name

    materialize { (frame, name, alias) =>

      val columnName = alias match {
        case None |
             Some(`reserved_root_level_alias`) => fieldName
        case Some(alias) => s"$alias.$fieldName"
      }

      val hColumns = hCleaner.value.clean(frame, Some(columnName), alias = Some(fieldName))
      val tColumns = tCleaner.clean(frame, name, alias)
      hColumns ::: tColumns
    }
  }
}