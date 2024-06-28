
package io.shardingcat.model;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;

import io.shardingcat.util.ExecutorUtil;
import jsr166y.LinkedTransferQueue;

/**
 * @author shardingcat
 */
public class M2 {
    private long count;
    private final ThreadPoolExecutor x;
    private final BlockingQueue<TransferObject> y;

    public M2() {
        this.x = ExecutorUtil.create("B", 1);
        this.y = new LinkedTransferQueue<TransferObject>();
    }

    public long getCount() {
        return count;
    }

    public ThreadPoolExecutor getX() {
        return x;
    }

    public BlockingQueue<TransferObject> getY() {
        return y;
    }

    public void start() {
        new Thread(new A(), "A").start();
        new Thread(new C(), "C").start();
    }

    private final class A implements Runnable {
        @Override
        public void run() {
            for (;;) {
                try {
                    Thread.sleep(200L);
                } catch (InterruptedException e) {
                }
                for (int i = 0; i < 1000000; i++) {
                    final TransferObject t = new TransferObject();
                    x.execute(new Runnable() {
                        @Override
                        public void run() {
                            t.handle();
                            y.offer(t);
                        }
                    });
                }
            }
        }
    }

    private final class C implements Runnable {
        @Override
        public void run() {
            TransferObject t = null;
            for (;;) {
                try {
                    t = y.take();
                } catch (InterruptedException e) {
                    continue;
                }
                t.compelete();
                count++;
            }
        }
    }

}