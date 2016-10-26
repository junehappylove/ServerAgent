# ServerAgent
 持续改进的性能监控工具，由原JMeter的性能监控插件逆向而来

> 1. [2016-10-26]增加了对jmx监控cpu使用率的支持。命令：metrics:jmx:cpuused-\n
> 2. [....-..-..]ServerAgent 所有的监控项目，以及使用命令可参见：http://www.jmeter-plugins.org/wiki/PerfMonMetrics/#JMX-Metrics

## 关于如何设置JMX监控的说明：
 详情请参见我的csdn博客：http://blog.csdn.net/junehappylove/article/details/52938170
,注意: jmx设置监控的端口号，ServerAgent这个工具的监控端口默认是 **4711** ，地址为本机 localhost
 
## 使用方法
> 1. 切换到dist发布版分支，挡下来所有文件
> 2. window下运行 startAgent.bat
> 3. Linux下运行startAgent.sh
