
package io.shardingcat.heartbeat;

/**
 * @author shardingcat
 */
public class HeartbeatContext {

    // private final static long TIMER_PERIOD = 1000L;
    //
    // private String name;
    // private Timer timer;
    // private NIOProcessor[] processors;
    // private NIOConnector connector;
    //
    // public HeartbeatContext(String name) throws IOException {
    // this.name = name;
    // this.init();
    // }
    //
    // public void startup() {
    // // startup timer
    // timer.schedule(new TimerTask() {
    // @Override
    // public void run() {
    // TimeUtil.update();
    // }
    // }, 0L, TimeUtil.UPDATE_PERIOD);
    //
    // // startup processors
    // for (int i = 0; i < processors.length; i++) {
    // processors[i].startup();
    // }
    //
    // // startup connector
    // connector.start();
    // }
    //
    // public void doHeartbeat(HeartbeatConfig heartbeat) {
    // timer.schedule(new MySQLHeartbeatTask(connector, heartbeat), 0L,
    // TIMER_PERIOD);
    // }
    //
    // private void init() throws IOException {
    // // init timer
    // this.timer = new Timer(name + "Timer", false);
    //
    // // init processors
    // processors = new
    // NIOProcessor[Runtime.getRuntime().availableProcessors()];
    // for (int i = 0; i < processors.length; i++) {
    // processors[i] = new NIOProcessor(name + "Processor" + i);
    // }
    //
    // // init connector
    // connector = new NIOConnector(name + "Connector");
    // connector.setProcessors(processors);
    // }

}