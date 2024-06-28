
package io.shardingcat.route.function;

import org.junit.Assert;
import org.junit.Test;


public class PartitionByCRC32PreSlotTest {

	@Test
	public void test()  {
		PartitionByCRC32PreSlot partition = new PartitionByCRC32PreSlot();
		 partition.setRuleName("test");
		partition.setCount(1000);
		partition.reInit();

		Assert.assertEquals(true, 521 == partition.calculate("1000316"));
		Assert.assertEquals(true, 637 == partition.calculate("2"));


		partition.setCount(2);
		partition.reInit();

		Assert.assertEquals(true, 0 == partition.calculate("1"));
		Assert.assertEquals(true, 1 == partition.calculate("2"));
		Assert.assertEquals(true, 0 == partition.calculate("3"));
		Assert.assertEquals(true, 1 == partition.calculate("4"));
		Assert.assertEquals(true, 0 == partition.calculate("5"));
		Assert.assertEquals(true, 0 == partition.calculate("6"));
		Assert.assertEquals(true, 0 == partition.calculate("7"));
		Assert.assertEquals(true, 0 == partition.calculate("8"));
		Assert.assertEquals(true, 0 == partition.calculate("9"));

		Assert.assertEquals(true, 0 == partition.calculate("9999"));
		Assert.assertEquals(true, 1 == partition.calculate("123456789"));
		Assert.assertEquals(true, 1 == partition.calculate("35565"));


		partition.setCount(3);
		partition.reInit();

		Assert.assertEquals(true, 1 == partition.calculate("1"));
		Assert.assertEquals(true, 1 == partition.calculate("2"));
		Assert.assertEquals(true, 0 == partition.calculate("3"));
		Assert.assertEquals(true, 2 == partition.calculate("4"));
		Assert.assertEquals(true, 0 == partition.calculate("5"));
		Assert.assertEquals(true, 1 == partition.calculate("6"));
		Assert.assertEquals(true, 1 == partition.calculate("7"));
		Assert.assertEquals(true, 0 == partition.calculate("8"));
		Assert.assertEquals(true, 0 == partition.calculate("9"));

		Assert.assertEquals(true, 0 == partition.calculate("9999"));
		Assert.assertEquals(true, 2 == partition.calculate("123456789"));
		Assert.assertEquals(true, 2 == partition.calculate("35565"));
	}

	public static void main(String[] args) {

		for (int i=0;i<20;i++)
		{
			int y=9;
			int count=3;
			long 	slot=i%y;
			int slotSize=  y/count;

			Long index = slot / slotSize;
			 if(slotSize*count!=y&&index>count-1)
			 {
			 	index=index-1;
			 }
			System.out.println(slot+"   "+index);
		}
	}
}
