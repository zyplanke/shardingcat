
package io.shardingcat.util;

import org.junit.Test;

/**
 * @author shardingcat
 */
public class BitTest {
    @Test
    public void testNoop() {
    }

    public static void main(String[] args) {
        System.out.println(0xffff0001 & 0xffff);// 低16位
        System.out.println(0x0002ffff >>> 16);// 高16位
    }
}