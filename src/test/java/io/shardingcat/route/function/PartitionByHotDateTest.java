
package io.shardingcat.route.function;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.Assert;
import org.junit.Test;

public class PartitionByHotDateTest {

	@Test
	public void test()  {
PartitionByHotDate partition = new PartitionByHotDate();
		
		partition.setDateFormat("yyyy-MM-dd");
		partition.setsLastDay("10");
		partition.setsPartionDay("1");

		partition.init();
		
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

		Calendar cDate = Calendar.getInstance();
		cDate.set(Calendar.MONTH, cDate.get(Calendar.MONTH));
		cDate.set(Calendar.DATE, cDate.get(Calendar.DATE));
		Assert.assertEquals(true, 0 == partition.calculate(dateFormat.format(cDate.getTime())));
		
		cDate = Calendar.getInstance();
		cDate.add(Calendar.DATE,-5);
		System.err.println(dateFormat.format(cDate.getTime()));
		Assert.assertEquals(true, 0 == partition.calculate(dateFormat.format(cDate.getTime())));
		
		cDate = Calendar.getInstance();
		cDate.add(Calendar.DATE,-11);
		System.err.println(dateFormat.format(cDate.getTime()));
		Assert.assertEquals(true, 2 == partition.calculate(dateFormat.format(cDate.getTime())));
		
		cDate = Calendar.getInstance();
		cDate.add(Calendar.DATE, -21);
		System.err.println(dateFormat.format(cDate.getTime()));
		Assert.assertEquals(true, 12 == partition.calculate(dateFormat.format(cDate.getTime())));

		cDate = Calendar.getInstance();
		cDate.add(Calendar.DATE,-5);
		System.err.println(dateFormat.format(cDate.getTime()));
		Assert.assertEquals(true, 0 == partition.calculateRange(dateFormat.format(cDate.getTime()),dateFormat.format(Calendar.getInstance().getTime()))[0]);
		
		cDate = Calendar.getInstance();
		cDate.add(Calendar.DATE,-11);
		System.err.println(dateFormat.format(cDate.getTime()));
		Assert.assertEquals(true, 0 == partition.calculateRange(dateFormat.format(cDate.getTime()),dateFormat.format(Calendar.getInstance().getTime()))[0]);
		Assert.assertEquals(true, 1 == partition.calculateRange(dateFormat.format(cDate.getTime()),dateFormat.format(Calendar.getInstance().getTime()))[1]);
		Assert.assertEquals(true, 2 == partition.calculateRange(dateFormat.format(cDate.getTime()),dateFormat.format(Calendar.getInstance().getTime()))[2]);

		cDate = Calendar.getInstance();
		cDate.add(Calendar.DATE, -21);
		System.err.println(dateFormat.format(cDate.getTime()));
		Assert.assertEquals(true, 0 == partition.calculateRange(dateFormat.format(cDate.getTime()),dateFormat.format(Calendar.getInstance().getTime()))[0]);
		Assert.assertEquals(true, 1 == partition.calculateRange(dateFormat.format(cDate.getTime()),dateFormat.format(Calendar.getInstance().getTime()))[1]);
		Assert.assertEquals(true, 2 == partition.calculateRange(dateFormat.format(cDate.getTime()),dateFormat.format(Calendar.getInstance().getTime()))[2]);
		Assert.assertEquals(true, 12 == partition.calculateRange(dateFormat.format(cDate.getTime()),dateFormat.format(Calendar.getInstance().getTime()))[12]);
		Assert.assertEquals(true, 13 == partition.calculateRange(dateFormat.format(cDate.getTime()),dateFormat.format(Calendar.getInstance().getTime())).length);

	}
}