
package io.shardingcat.model;

/**
 * @author shardingcat
 */
public class M2Main {

    public static void main(String[] args) {
        final M2 m2 = new M2();
        m2.start();

        new Thread() {
            @Override
            public void run() {
                for (;;) {
                    long c = m2.getCount();
                    try {
                        Thread.sleep(2000L);
                    } catch (InterruptedException e) {
                        continue;
                    }
                    System.out.println("tps:" + (m2.getCount() - c) / 2);
                    System.out.println("  x:" + m2.getX().getQueue().size());
                    System.out.println("  y:" + m2.getY().size());
                    System.out.println("==============");
                }
            }
        }.start();
    }

}