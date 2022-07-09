## run-line-agent

收集程序运行过哪些行

```
-javaagent:/path/run-line-agent.jar=${workspace};${project};${branch};${packages};${server}
```
>`workspace`：文件目录
>`project`：项目名
>`branch`：分支名称
>`packages`：要统计的包名称，多个,隔开
>`server`：run-line-server 启动地址 host:port

RunLine Data
```
/${workspace}/.runline/${project}/${branch}/${packages}
```

Source Code
```
/${workspace}/runline/${project}
```