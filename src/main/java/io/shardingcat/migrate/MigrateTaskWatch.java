package io.shardingcat.migrate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import io.shardingcat.ShardingCatServer;
import io.shardingcat.config.loader.zkprocess.comm.ZkConfig;
import io.shardingcat.config.loader.zkprocess.comm.ZkParamCfg;
import io.shardingcat.util.ZKUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * ......./migrate/schemal/taskid/datahost   [任务数据]
 * Created by magicdoom on 2016/9/28.
 */
public class MigrateTaskWatch {
    private static final Logger LOGGER = LoggerFactory.getLogger(MigrateTaskWatch.class);
    public static void start()
    {
        ZKUtils.addChildPathCache(ZKUtils.getZKBasePath() + "migrate", new PathChildrenCacheListener() {
                @Override public void childEvent(CuratorFramework curatorFramework,
                        PathChildrenCacheEvent fevent) throws Exception {

                    switch (fevent.getType()) {
                        case CHILD_ADDED:
                            LOGGER.info("table CHILD_ADDED: " + fevent.getData().getPath());
                         ZKUtils.addChildPathCache(fevent.getData().getPath(),new TaskPathChildrenCacheListener()) ;
                            break;
                        default:
                            break;
                    }
                }
            });

    }






    private static class TaskPathChildrenCacheListener implements PathChildrenCacheListener {
        @Override public void childEvent(CuratorFramework curatorFramework,
                PathChildrenCacheEvent event) throws Exception {
            switch (event.getType()) {
                case CHILD_ADDED:
                    if(isTaskErrorOrSucess(event))break;
                    addOrUpdate(event);
                    String path = event.getData().getPath() + "/_prepare";
                   if( curatorFramework.checkExists().forPath(path)==null){
                       curatorFramework.create().creatingParentsIfNeeded().forPath(path);
                   }
                   ZKUtils.addChildPathCache(path,new SwitchPrepareListener());

                    String commitPath = event.getData().getPath() + "/_commit";
                    if( curatorFramework.checkExists().forPath(commitPath)==null){
                        curatorFramework.create().creatingParentsIfNeeded().forPath(commitPath);
                    }
                    ZKUtils.addChildPathCache(commitPath,new SwitchCommitListener());


                    String cleanPath = event.getData().getPath() + "/_clean";
                    if( curatorFramework.checkExists().forPath(cleanPath)==null){
                        curatorFramework.create().creatingParentsIfNeeded().forPath(cleanPath);
                    }
                    ZKUtils.addChildPathCache(cleanPath,new SwitchCleanListener());
                    LOGGER.info("table CHILD_ADDED: " + event.getData().getPath());
                    break;
                case CHILD_UPDATED:
                    if(isTaskErrorOrSucess(event))break;
                    addOrUpdate(event);
                    LOGGER.info("CHILD_UPDATED: " + event.getData().getPath());
                    break;
                default:
                    break;
            }
        }

    private boolean isTaskErrorOrSucess(PathChildrenCacheEvent event){
        try {
            TaskNode pTaskNode= JSON.parseObject(event.getData().getData(),TaskNode.class);
            if(pTaskNode.getStatus()>=4) {
                return true;
            }
        } catch (Exception e){

        }

        return false;
    }

        private void addOrUpdate(PathChildrenCacheEvent event) throws Exception {

            InterProcessMutex taskLock =null;
            try{
               String tpath=    event.getData().getPath();
                String taskID=tpath.substring(tpath.lastIndexOf("/")+1,tpath.length());
                String lockPath=     ZKUtils.getZKBasePath()+"lock/"+taskID+".lock";
                taskLock=	 new InterProcessMutex(ZKUtils.getConnection(), lockPath);
                taskLock.acquire(20, TimeUnit.SECONDS);
             String text = new String(ZKUtils.getConnection().getData().forPath(event.getData().getPath()), "UTF-8");

            List<String> dataNodeList= ZKUtils.getConnection().getChildren().forPath(event.getData().getPath());
            if(!dataNodeList.isEmpty()) {
                if ((!Strings.isNullOrEmpty(text) )&& text.startsWith("{")) {
                    TaskNode taskNode = JSON.parseObject(text, TaskNode.class);
                    if (taskNode.getStatus() == 0) {
                        String boosterDataHosts = ZkConfig.getInstance().getValue(ZkParamCfg.SHARDINGCAT_BOOSTER_DATAHOSTS);
                        Set<String> dataNodes = new HashSet<>(Splitter.on(",").trimResults().omitEmptyStrings().splitToList(boosterDataHosts));
                        List<MigrateTask> finalMigrateList = new ArrayList<>();
                        for (String s : dataNodeList) {
                            if ("_prepare".equals(s))
                                continue;
                            if (dataNodes.contains(s)) {
                                String zkpath = event.getData().getPath() + "/" + s;
                                String data = new String(ZKUtils.getConnection().getData().forPath(zkpath), "UTF-8");
                                List<MigrateTask> migrateTaskList = JSONArray.parseArray(data, MigrateTask.class);
                                for (MigrateTask migrateTask : migrateTaskList) {
                                    migrateTask.setZkpath(zkpath);
                                }
                                finalMigrateList.addAll(migrateTaskList);
                            }
                        }

                        Map<String, List<MigrateTask>> taskMap = mergerTaskForDataHost(finalMigrateList);
                        for (Map.Entry<String, List<MigrateTask>> stringListEntry : taskMap.entrySet()) {
                            String key = stringListEntry.getKey();
                            List<MigrateTask> value = stringListEntry.getValue();
                            ShardingCatServer.getInstance().getBusinessExecutor().submit(new MigrateMainRunner(key, value));
                        }

                        //
                        taskNode.setStatus(1);
                        ZKUtils.getConnection().setData().forPath(event.getData().getPath(), JSON.toJSONBytes(taskNode));
                    } else if (taskNode.getStatus() == 2) {
                        //start switch

                        ScheduledExecutorService scheduledExecutorService = ShardingCatServer.getInstance().getScheduler();
                        Set<String> allRunnerSet = SwitchPrepareCheckRunner.allSwitchRunnerSet;
                        if (!allRunnerSet.contains(taskID)) {
                            List<String> dataHosts = ZKUtils.getConnection().getChildren().forPath(tpath);
                            List<MigrateTask> allTaskList = MigrateUtils.queryAllTask(tpath, removeStatusNode(dataHosts));
                            allRunnerSet.add(taskID);
                            scheduledExecutorService.schedule(new SwitchPrepareCheckRunner(taskID, tpath, taskNode,
                                    MigrateUtils.convertAllTask(allTaskList)), 1, TimeUnit.SECONDS);

                        }
                    }
                }
            }
            }finally {
                 if(taskLock!=null){
                     taskLock.release();
                 }
            }
        }

        private List<String> removeStatusNode(List<String> dataHosts){
            List<String> resultList=new ArrayList<>();
            for (String dataHost : dataHosts) {
                if("_prepare".equals(dataHost)||"_commit".equals(dataHost)||"_clean".equals(dataHost))
                {
                    continue;
                }
                resultList.add(dataHost);
            }



            return resultList;
        }


        private static String getDataHostNameFromNode(String dataNode){
            return ShardingCatServer.getInstance().getConfig().getDataNodes().get(dataNode).getDbPool().getHostName();
        }

        private static   Map<String, List<MigrateTask> > mergerTaskForDataHost ( List<MigrateTask> migrateTaskList)
        {
            Map<String, List<MigrateTask> > taskMap=new HashMap<>();
            for (MigrateTask migrateTask : migrateTaskList) {
                String dataHost=getDataHostNameFromNode(migrateTask.getFrom());
                if(taskMap.containsKey(dataHost)) {
                    taskMap.get(dataHost).add(migrateTask);
                }   else
                {
                    taskMap.put(dataHost, Lists.newArrayList(migrateTask)) ;
                }
            }


            return taskMap;
        }



    }
}
