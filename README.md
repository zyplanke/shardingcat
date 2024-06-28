# shardingcat分库分表中间件
初始参考Mycat版本: 2018-01-31 (GitVersion:0c56ebd34dcdc5594d71765d614ea0cac0be64b7)

### 主要特色
- 增加对ARM适配支持(包括Linux和MacOSX的ARM 64bit平台)
- 移除了对32bit平台的支持，移除了对Solaris、HPUX的支持
- 进一步支持PostgreSQL，默认携带PostgreSQL驱动
- Mysql驱动升级至8.x
- 对代码、配置内容进行了调整和完善
- 修改Bug
- 对Maven各依赖、插件进行了升级、删除了不用的依赖，调整了配置
- 代码、配置改为shardingcat
- xml命名空间改为shardingcat
- 工程命名改为全小写，以符合规范
- 启动参数默认开启jvm gc的日志输出
- License调整为Apache License 2.0

### 编译命令示例
确保本机已装Maven，通过如下命令打包（若不指定-Dmaven.repo.local，则默认使用Maven setting.xml文件中所配置的本地仓位置）
```
${MAVEN_HOME}/bin/mvn -Dmaven.repo.local="D:\IdeaProjects\repository" clean package
```
生成的结果在target子目录下，其中包括已打包为可发布的*.tar.gz文件
