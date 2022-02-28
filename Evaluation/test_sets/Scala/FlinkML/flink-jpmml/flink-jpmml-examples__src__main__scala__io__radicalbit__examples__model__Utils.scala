/*
 * Copyright (C) 2017  Radicalbit
 *
 * This file is part of flink-JPMML
 *
 * flink-JPMML is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * flink-JPMML is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with flink-JPMML.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.radicalbit.examples.model

import java.util.UUID

import io.radicalbit.flink.pmml.scala.models.core.ModelId

object Utils {

  final val modelVersion = 1.toString

  def retrieveMappingIdPath(modelPaths: Seq[String]): Map[String, String] =
    modelPaths.map(path => (UUID.randomUUID().toString, path)).toMap

  def retrieveAvailableId(mappingIdPath: Map[String, String]): Seq[String] =
    mappingIdPath.keys.map(name => name + ModelId.separatorSymbol + modelVersion).toSeq

  def now(): Long = System.currentTimeMillis()
}
