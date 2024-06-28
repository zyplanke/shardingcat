# shardingcat分库分表中间件
初始参考Mycat版本: 2018-01-31 (GitVersion:0c56ebd34dcdc5594d71765d614ea0cac0be64b7)

### 编译命令示例
确保本机已装Maven，通过如下命令打包（若不指定-Dmaven.repo.local，则默认使用Maven setting.xml文件中所配置的本地仓位置）
```
${MAVEN_HOME}/bin/mvn -Dmaven.repo.local="D:\IdeaProjects\repository" clean package
```
生成的结果在target子目录下，其中包括已打包为可发布的*.tar.gz文件
