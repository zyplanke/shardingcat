
package io.shardingcat.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author shardingcat
 */
public class StringUtilTest {

    @Test
    public void test() {
        String oriSql = "insert into ssd  (id) values (s)";
        String tableName = StringUtil.getTableName(oriSql);
        Assert.assertEquals("ssd", tableName);
    }

    @Test
    public void test1() {
    	String oriSql = "insert into    ssd(id) values (s)";
        String tableName = StringUtil.getTableName(oriSql);
        Assert.assertEquals("ssd", tableName);
    }

    @Test
    public void test2() {
    	String oriSql = "  insert  into    ssd(id) values (s)";
        String tableName = StringUtil.getTableName(oriSql);
        Assert.assertEquals("ssd", tableName);
    }

    @Test
    public void test3() {
    	String oriSql = "  insert  into    isd(id) values (s)";
        String tableName = StringUtil.getTableName(oriSql);
        Assert.assertEquals("isd", tableName);
    }

    @Test
    public void test4() {
    	String oriSql = "INSERT INTO test_activity_input  (id,vip_no";
        String tableName = StringUtil.getTableName(oriSql);
        Assert.assertEquals("test_activity_input", tableName);
    }
    
    @Test
    public void test5() {
    	String oriSql = " /* ApplicationName=DBeaver 3.3.1 - Main connection */ insert into employee(id,name,sharding_id) values(4, 'myhome', 10011)";
        String tableName = StringUtil.getTableName(oriSql);
        Assert.assertEquals("employee", tableName);
    }
    
    @Test
    public void test6() {
    	String oriSql = " /* insert int a (id, name) value(1, 'ben') */ insert into employee(id,name,sharding_id) values(4, 'myhome', 10011)";
        String tableName = StringUtil.getTableName(oriSql);
        Assert.assertEquals("employee", tableName);
    }
    
    @Test
    public void test7() {
    	String oriSql = " /**/ insert into employee(id,name,sharding_id) values(4, 'myhome', 10011)";
        String tableName = StringUtil.getTableName(oriSql);
        Assert.assertEquals("employee", tableName);
    }
    
    @Test
    public void test8() {
    	String oriSql = " /*  */ insert into employee(id,name,sharding_id) values(4, 'myhome', 10011) /**/";
        String tableName = StringUtil.getTableName(oriSql);
        Assert.assertEquals("employee", tableName);
    }
    
    @Test
    public void test9() {
    	String oriSql = " /* hint1 insert */ /**/ /* hint3 insert */ insert into employee(id,name,sharding_id) values(4, 'myhome', 10011) /**/";
        String tableName = StringUtil.getTableName(oriSql);
        Assert.assertEquals("employee", tableName);
    }
    
    @Test
    public void test10() {
    	String oriSql = " /* hint1 insert */ /* // */ /* hint3 insert */ insert into employee(id,name,sharding_id) values(4, 'myhome', 10011) /**/";
        String tableName = StringUtil.getTableName(oriSql);
        Assert.assertEquals("employee", tableName);
    }
    
    @Test
    public void test11() {
    	String oriSql = " /* hint1 insert */ /* // */ /* hint3 insert */ insert /*  */ into employee(id,name,sharding_id) values(4, 'myhome', 10011) /**/";
        String tableName = StringUtil.getTableName(oriSql);
        Assert.assertEquals("employee", tableName);
    }
    @Test
    public void test12() {
    	StringWriter sw=new StringWriter();
    	PrintWriter pw=new PrintWriter(sw);
    	pw.println("insert into");
    	pw.println(" employee(id,name,sharding_id) values(4, 'myhome', 10011)");
    	pw.flush();
    	String oriSql = sw.toString();
        String tableName = StringUtil.getTableName(oriSql);
        Assert.assertEquals("employee", tableName);
    }
    @Test
    public void test13() {
    	StringWriter sw=new StringWriter();
    	PrintWriter pw=new PrintWriter(sw);
    	pw.println("insert into");
    	pw.println("employee(id,name,sharding_id) values(4, 'myhome', 10011)");
    	pw.flush();
    	String oriSql = sw.toString();
        String tableName = StringUtil.getTableName(oriSql);
        Assert.assertEquals("employee", tableName);
    }
}