
package io.shardingcat.util;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author shardingcat
 */
public class SplitUtilTest {

    @Test
    public void test() {
        String str = "mysql$1-3,mysql7,mysql9";
        String[] destStr = SplitUtil.split(str, ',', '$', '-');
        Assert.assertEquals(5, destStr.length);
        Assert.assertEquals("mysql1", destStr[0]);
        Assert.assertEquals("mysql2", destStr[1]);
        Assert.assertEquals("mysql3", destStr[2]);
        Assert.assertEquals("mysql7", destStr[3]);
        Assert.assertEquals("mysql9", destStr[4]);
    }

    @Test
    public void test1() {
        String src = "offer$0-3";
        String[] dest = SplitUtil.split(src, '$', true);
        Assert.assertEquals(2, dest.length);
        Assert.assertEquals("offer", dest[0]);
        Assert.assertEquals("0-3", dest[1]);
    }

    @Test
    public void test2() {
        String src = "OFFER_group";
        String[] dest = SplitUtil.split2(src, '$', '-');
        Assert.assertEquals(1, dest.length);
        Assert.assertEquals("OFFER_group", dest[0]);
    }

    @Test
    public void test3() {
        String src = "OFFER_group$2";
        String[] dest = SplitUtil.split2(src, '$', '-');
        Assert.assertEquals(1, dest.length);
        Assert.assertEquals("OFFER_group[2]", dest[0]);
    }

    @Test
    public void test4() {
        String src = "offer$0-3";
        String[] dest = SplitUtil.split2(src, '$', '-');
        Assert.assertEquals(4, dest.length);
        Assert.assertEquals("offer[0]", dest[0]);
        Assert.assertEquals("offer[1]", dest[1]);
        Assert.assertEquals("offer[2]", dest[2]);
        Assert.assertEquals("offer[3]", dest[3]);
    }

}