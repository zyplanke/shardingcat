# shardingcat分库分表中间件

### shardingcat主要特色
- 支持对ARM适配支持(包括Linux和MacOSX的ARM 64bit平台)
- 移除对32bit平台的支持，移除了对Solaris、HPUX的支持
- 支持PostgreSQL，默认携带PostgreSQL驱动
- Mysql驱动升级至8.x
- 对代码、配置内容进行了调整和完善
- 对Maven各依赖、插件进行了升级、删除了不用的依赖，调整了build配置
- 代码、配置为shardingcat
- xml命名空间为shardingcat
- 工程命名等符合规范
- 启动参数默认开启jvm gc的日志输出
- License调整为Apache License 2.0

初始参考自mycat的2018-01-31版本GitVersion:0c56ebd34dcdc5594d71765d614ea0cac0be64b7

### 编译命令示例
确保本机已装Maven，通过如下命令打包（若不指定-Dmaven.repo.local，则默认使用Maven setting.xml文件中所配置的本地仓位置）
```
${MAVEN_HOME}/bin/mvn -Dmaven.repo.local="D:\IdeaProjects\repository" clean package

# 若不需要执行单元测试
${MAVEN_HOME}/bin/mvn -Dmaven.repo.local="D:\IdeaProjects\repository" -DskipTests clean package
```
生成的结果在target子目录下，其中包括已打包为可发布的*.tar.gz文件

### 部署
将上步已打包为可发布的*.tar.gz文件，选择对应平台，拷贝到目标服务器后进行解压。
解压得到目录结构包括bin、conf、lib、catlet、logs、gclogs子目录。
其中lib子目录下为依赖的so库和各个jar，自带后端数据库连接驱动PostgreSQL和MySQL（若不需要可手工删除连接驱动）
解压后，可通过以下命令运行：
```
./bin/shardingcat start
```
