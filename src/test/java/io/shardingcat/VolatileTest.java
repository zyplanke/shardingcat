
package io.shardingcat;

import org.junit.Test;

/**
 * @author shardingcat
 */
public class VolatileTest {
    @Test
    public void testNoop() {
    }

    static class VolatileObject {
        volatile Object object = new Object();
    }

    public static void main(String[] args) {
        final VolatileObject vo = new VolatileObject();

        // set
        new Thread() {
            @Override
            public void run() {
                System.out.print("set...");
                while (true) {
                    vo.object = new Object();
                }
            }
        }.start();

        // get
        new Thread() {
            @Override
            public void run() {
                System.out.print("get...");
                while (true) {
                    Object oo = vo.object;
                    oo.toString();
                }
            }
        }.start();
    }

}