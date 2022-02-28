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

import org.apache.spark.sql.SPLLogging
import org.scalatest.BeforeAndAfterAll

class FilterTest extends BaseTest with BeforeAndAfterAll with SPLLogging {

  test("inclauseTest1",
    "select c_name, sum(c_acctbal) as bal " +
      "from orderLineItemPartSupplier " +
      "where c_mktsegment in ('MACHINERY', 'HOUSEHOLD') " +
      "group by c_name",
    1,
    true)

  test("inclauseTest2",
    "select c_name, sum(c_acctbal) as bal " +
      "from orderLineItemPartSupplier " +
      "where l_linenumber in (1, 2, 3, 4, 5, 6, 7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22," +
      "23,24,25,26,27,28,29,30,31,32,33,34,35,36,37,38,39,40,41,42,43,44,45) " +
      "group by c_name",
    1,
    true)

  test("notInclauseTest1",
    "select c_name, sum(c_acctbal) as bal " +
      "from orderLineItemPartSupplier " +
      "where c_mktsegment not in ('MACHINERY', 'HOUSEHOLD') " +
      "group by c_name",
    1,
    true
  )

  test("notEqTest1",
    "select c_name, sum(c_acctbal) as bal " +
      "from orderLineItemPartSupplier " +
      "where c_mktsegment !=  'MACHINERY' " +
      "group by c_name",
    1,
    true
  )

  test("compTestGTE",
    "select s_nation, sum(c_acctbal) as bal " +
      "from orderLineItemPartSupplier " +
      "where s_nation >= 'FRANCE' " +
      "group by s_nation order by s_nation limit 2",
    1,
    true,
    true
  )

  test("compTestGT",
    "select s_nation, sum(c_acctbal) as bal " +
      "from orderLineItemPartSupplier " +
      "where s_nation > 'FRANCE' " +
      "group by s_nation order by s_nation limit 2",
    1,
    true,
    true
  )

  test("compTestLTE",
    "select s_nation, sum(c_acctbal) as bal " +
      "from orderLineItemPartSupplier " +
      "where s_nation <= 'FRANCE' " +
      "group by s_nation order by s_nation desc limit 2",
    1,
    true,
    true
  )

  test("compTestLT",
    "select s_nation, sum(c_acctbal) as bal " +
      "from orderLineItemPartSupplier " +
      "where s_nation < 'FRANCE' " +
      "group by s_nation order by s_nation desc limit 2",
    1,
    true,
    true
  )

  test("compTestLTNumeric",
    "select p_size, sum(c_acctbal) as bal " +
      "from orderLineItemPartSupplier " +
      "where p_size < 50 " +
      "group by p_size order by p_size desc limit 10",
    1,
    true,
    true
  )

  test("compTestGTENumeric",
    "select p_size, sum(c_acctbal) as bal " +
      "from orderLineItemPartSupplier " +
      "where p_size >= 40 " +
      "group by p_size order by p_size limit 10",
    1,
    true,
    true
  )

  test("compTestBetween",
    "select p_size, sum(c_acctbal) as bal " +
      "from orderLineItemPartSupplier " +
      "where p_size between 40 and 50 " +
      "group by p_size order by p_size limit 10",
    1,
    true,
    true
  )

  test("isNotNullTest1",
    "select s_region from orderLineItemPartSupplier" +
      " where l_shipdate is not null and l_discount is not null and p_name is not null" +
      " group by s_region " +
      " order by s_region",
    1,
    true, true)


  test("isNotNullTest2",
    "select s_region from orderLineItemPartSupplier" +
      " where (cast(l_shipdate as double) + 10) is not null and " +
      " (l_discount % 10) is not null and" +
      " cast(c_phone as int)  is not null and" +
      " p_name is not null" +
      " group by s_region " +
      " order by s_region",
    1,
    true, true)

  test("notIsNullTest1",
    "select s_region from orderLineItemPartSupplier" +
      " where not(l_shipdate is null) and not(l_discount is null) and not(p_name is null)" +
      " group by s_region " +
      " order by s_region",
    1,
    true, true)


  test("notIsNullTest2",
    "select s_region from orderLineItemPartSupplier" +
      " where not((cast(l_shipdate as double) + 10) is null) and " +
      " not((l_discount % 10) is null) and not(cast(c_phone as int)  is null)" +
      " and not(p_name  is null)" +
      " group by s_region " +
      " order by s_region",
    1,
    true, true)

  test("isNullTest1",
    "select s_region from orderLineItemPartSupplier" +
      " where (l_shipdate is null) and (p_name is null)" +
      " group by s_region " +
      " order by s_region",
    1,
    true, true)
}
