
package io.shardingcat.model;

/**
 * @author shardingcat
 */
public class M1Main {

    public static void main(String[] args) {
        final M1 m1 = new M1();
        m1.start();

        new Thread() {
            @Override
            public void run() {
                for (;;) {
                    long c = m1.getCount();
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException e) {
                        continue;
                    }
                    System.out.println("tps:" + (m1.getCount() - c) / 2);
                    System.out.println("  x:" + m1.getX().size());
                    System.out.println("  y:" + m1.getY().size());
                    System.out.println("==============");
                }
            }
        }.start();
    }

}