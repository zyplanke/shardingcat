
package io.shardingcat.route.function;

import org.junit.Assert;
import org.junit.Test;

public class PartitionByMonthTest {

	@Test
	public void test()  {
		PartitionByMonth partition = new PartitionByMonth();

		partition.setDateFormat("yyyy-MM-dd");
		partition.setsBeginDate("2014-01-01");

		partition.init();

		Assert.assertEquals(true, 0 == partition.calculate("2014-01-01"));
		Assert.assertEquals(true, 0 == partition.calculate("2014-01-10"));
		Assert.assertEquals(true, 0 == partition.calculate("2014-01-31"));
		Assert.assertEquals(true, 1 == partition.calculate("2014-02-01"));
		Assert.assertEquals(true, 1 == partition.calculate("2014-02-28"));
		Assert.assertEquals(true, 2 == partition.calculate("2014-03-1"));
		Assert.assertEquals(true, 11 == partition.calculate("2014-12-31"));
		Assert.assertEquals(true, 12 == partition.calculate("2015-01-31"));
		Assert.assertEquals(true, 23 == partition.calculate("2015-12-31"));

		partition.setDateFormat("yyyy-MM-dd");
		partition.setsBeginDate("2015-01-01");
		partition.setsEndDate("2015-12-01");

		partition.init();

		/**
		 *  0 : 2016-01-01~31, 2015-01-01~31, 2014-01-01~31
		 *  1 : 2016-02-01~28, 2015-02-01~28, 2014-02-01~28
		 *  5 : 2016-06-01~30, 2015-06-01~30, 2014-06-01~30
		 * 11 : 2016-12-01~31, 2015-12-01~31, 2014-12-01~31
		 */

		Assert.assertEquals(true, 0 == partition.calculate("2013-01-02"));
		Assert.assertEquals(true, 0 == partition.calculate("2014-01-01"));
		Assert.assertEquals(true, 0 == partition.calculate("2015-01-10"));
		Assert.assertEquals(true, 0 == partition.calculate("2015-01-31"));
		Assert.assertEquals(true, 0 == partition.calculate("2016-01-20"));

		Assert.assertEquals(true, 1 == partition.calculate("2013-02-02"));
		Assert.assertEquals(true, 1 == partition.calculate("2014-02-01"));
		Assert.assertEquals(true, 1 == partition.calculate("2015-02-10"));
		Assert.assertEquals(true, 1 == partition.calculate("2015-02-28"));
		Assert.assertEquals(true, 1 == partition.calculate("2016-02-20"));

		Assert.assertEquals(true, 5 == partition.calculate("2013-06-01"));
		Assert.assertEquals(true, 5 == partition.calculate("2014-06-01"));
		Assert.assertEquals(true, 5 == partition.calculate("2015-06-10"));
		Assert.assertEquals(true, 5 == partition.calculate("2015-06-28"));
		Assert.assertEquals(true, 5 == partition.calculate("2016-06-20"));

		Assert.assertEquals(true, 11 == partition.calculate("2013-12-28"));
		Assert.assertEquals(true, 11 == partition.calculate("2014-12-01"));
		Assert.assertEquals(true, 11 == partition.calculate("2014-12-31"));
		Assert.assertEquals(true, 11 == partition.calculate("2015-12-11"));
		Assert.assertEquals(true, 11 == partition.calculate("2016-12-31"));

	}
}
