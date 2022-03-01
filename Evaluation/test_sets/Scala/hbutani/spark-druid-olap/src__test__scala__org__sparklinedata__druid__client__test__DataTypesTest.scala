/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sparklinedata.druid.client.test

import org.apache.spark.sql.hive.test.sparklinedata.TestHive._
import org.sparklinedata.druid._

class DataTypesTest extends BaseTest {

  val starSchemaDataTypes =
    """
      |{
      |  "factTable" : "orderLineItemPartSupplier_datatypes",
      |  "relations" : []
      | }
    """.stripMargin.replace('\n', ' ')

  val starSchemaDataTypes2 =
    """
      |{
      |  "factTable" : "orderLineItemPartSupplier_datatypes2",
      |  "relations" : []
      | }
    """.stripMargin.replace('\n', ' ')

  override def beforeAll() = {
    super.beforeAll()

    /*
        o_orderdate date
        l_commitdate date
        l_receiptdate timestamp
     */
    val cT = s"""CREATE TABLE if not exists
             orderLineItemPartSupplierDataTypesBase(o_orderkey integer,
             o_custkey integer,
      o_orderstatus string, o_totalprice double, o_orderdate date, o_orderpriority string,
      o_clerk string,
      o_shippriority integer, o_comment string, l_partkey integer, l_suppkey integer,
      l_linenumber integer,
      l_quantity double, l_extendedprice double, l_discount double, l_tax double,
      l_returnflag string,
      l_linestatus string, l_shipdate string, l_commitdate date, l_receiptdate timestamp,
      l_shipinstruct string,
      l_shipmode string, l_comment string, order_year string, ps_partkey integer,
      ps_suppkey integer,
      ps_availqty integer, ps_supplycost double, ps_comment string, s_name string, s_address string,
      s_phone string, s_acctbal double, s_comment string, s_nation string,
      s_region string, p_name string,
      p_mfgr string, p_brand string, p_type string, p_size integer, p_container string,
      p_retailprice double,
      p_comment string, c_name string , c_address string , c_phone string , c_acctbal double ,
      c_mktsegment string , c_comment string , c_nation string , c_region string)
      USING com.databricks.spark.csv
      OPTIONS (path "src/test/resources/tpch/datascale1/orderLineItemPartSupplierCustomer.small",
      header "false", delimiter "|")""".stripMargin
    sql(cT)

    sql(
      s"""CREATE TABLE if not exists orderLineItemPartSupplier_datatypes
      USING org.sparklinedata.druid
      OPTIONS (sourceDataframe "orderLineItemPartSupplierDataTypesBase",
      timeDimensionColumn "l_shipdate",
      druidDatasource "tpch",
      druidHost '$zkConnectString',
      zkQualifyDiscoveryNames "true",
      queryHistoricalServers "false",
      columnMapping '$colMapping',
      numProcessingThreadsPerHistorical '1',
      functionalDependencies '$functionalDependencies',
      starSchema '$starSchemaDataTypes')""".stripMargin
    )

    val cT2 = s"""CREATE TABLE if not exists
             orderLineItemPartSupplierDataTypes2Base(o_orderkey integer,
             o_custkey integer,
      o_orderstatus string, o_totalprice double, o_orderdate date, o_orderpriority string,
      o_clerk string,
      o_shippriority integer, o_comment string, l_partkey integer, l_suppkey integer,
      l_linenumber integer,
      l_quantity double, l_extendedprice double, l_discount double, l_tax double,
      l_returnflag string,
      l_linestatus string, l_shipdate timestamp, l_commitdate date, l_receiptdate timestamp,
      l_shipinstruct string,
      l_shipmode string, l_comment string, order_year string, ps_partkey integer,
      ps_suppkey integer,
      ps_availqty integer, ps_supplycost double, ps_comment string, s_name string, s_address string,
      s_phone string, s_acctbal double, s_comment string, s_nation string,
      s_region string, p_name string,
      p_mfgr string, p_brand string, p_type string, p_size integer, p_container string,
      p_retailprice double,
      p_comment string, c_name string , c_address string , c_phone string , c_acctbal double ,
      c_mktsegment string , c_comment string , c_nation string , c_region string)
      USING com.databricks.spark.csv
      OPTIONS (path "src/test/resources/tpch/datascale1/orderLineItemPartSupplierCustomer.small",
      header "false", delimiter "|")""".stripMargin
    sql(cT2)

    sql(
      s"""CREATE TABLE if not exists orderLineItemPartSupplier_datatypes2
      USING org.sparklinedata.druid
      OPTIONS (sourceDataframe "orderLineItemPartSupplierDataTypes2Base",
      timeDimensionColumn "l_shipdate",
      druidDatasource "tpch",
      druidHost '$zkConnectString',
      zkQualifyDiscoveryNames "true",
      queryHistoricalServers "false",
      columnMapping '$colMapping',
      numProcessingThreadsPerHistorical '1',
      functionalDependencies '$functionalDependencies',
      starSchema '$starSchemaDataTypes2')""".stripMargin
    )

  }

  test("orderDate",
    "select o_orderdate, " +
      "count(*)  " +
      "from orderLineItemPartSupplier_datatypes group by o_orderdate",
    1,
    true,
    true
  )

  test("gbexprtest2",
    "select o_orderdate, " +
      "(substr(CAST(Date_Add(TO_DATE(CAST(CONCAT(o_orderdate, 'T00:00:00.000Z') " +
      "AS TIMESTAMP)), 5) AS TIMESTAMP), 0, 10)) x," +
      "sum(c_acctbal) as bal from orderLineItemPartSupplier_datatypes group by " +
      "o_orderdate, (substr(CAST(Date_Add(TO_DATE(CAST(CONCAT(TO_DATE(o_orderdate)," +
      " 'T00:00:00.000Z') AS TIMESTAMP)), 5) AS TIMESTAMP), 0, 10)) order by o_orderdate, x, bal",
    1,
    true, true)

  test("inclause-inTest1",
    s"""select c_name, sum(c_acctbal) as bal
      from orderLineItemPartSupplier_datatypes
      where to_Date(o_orderdate) >= cast('1993-01-01' as date) and to_Date(o_orderdate) <= cast('1997-12-31' as date)
       and cast(order_year as int) in (1993,1994,1995, null)
      group by c_name
      order by c_name, bal""".stripMargin,
    1,
    true, true)

  test("gbexprtest2-ts",
    "select o_orderdate, " +
      "(substr(CAST(Date_Add(TO_DATE(l_receiptdate), 5) AS TIMESTAMP), 0, 10)) x," +
      "sum(c_acctbal) as bal from orderLineItemPartSupplier_datatypes group by " +
      "o_orderdate, (substr(CAST(Date_Add(TO_DATE(l_receiptdate), 5) AS TIMESTAMP), 0, 10)) " +
      "order by o_orderdate, x, bal",
    1,
    true, true)

  test("inclause-inTest1-ts",
    s"""select c_name, sum(c_acctbal) as bal
      from orderLineItemPartSupplier_datatypes
      where to_Date(l_receiptdate) >= cast('1993-01-01' as date) and to_Date(l_receiptdate) <= cast('1997-12-31' as date)
       and cast(order_year as int) in (1993,1994,1995, null)
      group by c_name
      order by c_name, bal""".stripMargin,
    1,
    true, true)

  test("notPushOrderBy",
    s"""
       | select l_partkey, count(*)
       | from orderLineItemPartSupplier
       | group by l_partkey
       | order by l_partkey
     """.stripMargin,
    1,
    true,
    true,
    false,
    Seq({ dq : DruidQuery =>
      val gQ = dq.q.asInstanceOf[GroupByQuerySpec]
      !gQ.limitSpec.isDefined
    }))

  test("notPushOrderBy2",
    s"""
       | select l_partkey
       | from orderLineItemPartSupplier
       | group by l_partkey
       | order by l_partkey
     """.stripMargin,
    1,
    true,
    true,
    false,
    Seq({ dq : DruidQuery =>
      val sQ = dq.q.asInstanceOf[SearchQuerySpec]
      !sQ.sort.isDefined
    }))

  test("pushFilterAsJS",
    s"""
       | select l_partkey
       | from orderLineItemPartSupplier
       | where l_partkey > 10000
       | group by l_partkey
     """.stripMargin,
    1,
    true,
    true,
    false,
    Seq({ dq : DruidQuery =>
      val sQ = dq.q.asInstanceOf[SearchQuerySpec]
      val f = sQ.filter.get.asInstanceOf[LogicalFilterSpec]
      f.fields(1).isInstanceOf[JavascriptFilterSpec]
    }))


  test("metricTypeDifferent",
    """
      |SELECT DAY(`sp_demo_retail`.`o_orderdate`) AS `dy_invoicedate_ok`,
      |SUM(`sp_demo_retail`.`l_quantity`) AS `sum_quantity_ok`
      |FROM `default`.`orderLineItemPartSupplier` `sp_demo_retail`
      |GROUP BY DAY(`sp_demo_retail`.`o_orderdate`)
    """.stripMargin,
    1,
    true,
    true)

  // scalastyle:off line.size.limit
  test("timeColIsTimestampInSpark",
  """
    |SELECT CAST((MONTH(`sp_demo_retail`.`l_shipdate`) - 1) / 3 + 1 AS BIGINT) AS `qr_invoicedate_ok`,
    |YEAR(`sp_demo_retail`.`l_shipdate`) AS `yr_invoicedate_ok`
    |FROM `default`.`orderLineItemPartSupplier_datatypes2` `sp_demo_retail`
    |GROUP BY CAST((MONTH(`sp_demo_retail`.`l_shipdate`) - 1) / 3 + 1 AS BIGINT),
    |YEAR(`sp_demo_retail`.`l_shipdate`)
  """.stripMargin,
    1,
    true,
    true
  )

}
