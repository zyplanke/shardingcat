
package io.shardingcat.route.function;

import org.junit.Assert;
import org.junit.Test;

public class PartitionByDateTest {

	@Test
	public void test()  {
		PartitionByDate partition=new PartitionByDate();

		partition.setDateFormat("yyyy-MM-dd");
		partition.setsBeginDate("2014-01-01");
		partition.setsPartionDay("10");
		
		partition.init();
		
		Assert.assertEquals(true, 0 == partition.calculate("2014-01-01"));
		Assert.assertEquals(true, 0 == partition.calculate("2014-01-10"));
		Assert.assertEquals(true, 1 == partition.calculate("2014-01-11"));
		Assert.assertEquals(true, 12 == partition.calculate("2014-05-01"));
		
		partition.setDateFormat("yyyy-MM-dd");
		partition.setsBeginDate("2014-01-01");
		partition.setsEndDate("2014-01-31");
		partition.setsPartionDay("10");
		partition.init();
		
		/**
		 * 0 : 01.01-01.10,02.10-02.19
		 * 1 : 01.11-01.20,02.20-03.01
		 * 2 : 01.21-01.30,03.02-03.12
		 * 3  ： 01.31-02-09,03.13-03.23
		 */
		Assert.assertEquals(true, 0 == partition.calculate("2014-01-01"));
		Assert.assertEquals(true, 0 == partition.calculate("2014-01-10"));
		Assert.assertEquals(true, 1 == partition.calculate("2014-01-11"));
		Assert.assertEquals(true, 3 == partition.calculate("2014-02-01"));
		Assert.assertEquals(true, 0 == partition.calculate("2014-02-19"));
		Assert.assertEquals(true, 1 == partition.calculate("2014-02-20"));
		Assert.assertEquals(true, 1 == partition.calculate("2014-03-01"));
		Assert.assertEquals(true, 2 == partition.calculate("2014-03-02"));
		Assert.assertEquals(true, 2 == partition.calculate("2014-03-11"));
		Assert.assertEquals(true, 3 == partition.calculate("2014-03-20"));


	}
}