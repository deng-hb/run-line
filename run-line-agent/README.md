## run-line-agent

收集程序运行过哪些行

```
-javaagent:/path/run-line-agent.jar=${workspace};${project};${branch};${packages}
```

RunLine Data
```
/${workspace}/.runline/${project}/${branch}/${packages}
```

Source Code
```
/${workspace}/runline/${project}
```