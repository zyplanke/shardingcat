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

package io.shardingcat.memory.unsafe.utils.sort;

/**
 * Compares records for ordering. In cases where the entire sorting key can fit in the 8-byte
 * prefix, this may simply return 0.
 */
public abstract class RecordComparator {

  /**
   * Compare two records for order.
   *
   * @return a negative integer, zero, or a positive integer as the first record is less than,
   *         equal to, or greater than the second.
   */
  public abstract int compare(
    Object leftBaseObject,
    long leftBaseOffset,
    Object rightBaseObject,
    long rightBaseOffset);
}
