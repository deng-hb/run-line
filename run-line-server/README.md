## run-line-server

获取git代码
展示哪些代码运行过

使用jgit diff获取两个分支的每个文件变更的行数，与run-line-agent收集运行的行数做比较


### API


#### /projects
所有项目
```
[{"name":"run-line", "git": "git@xxx"}, ... ]
```

#### /project/${name}/${path ... }
项目树，一次返回两级
```
{"com/denghb": ["runline/server", "App.java"]}
```

#### /project/${name}/${path}/${filename}.java
文件内容列表
```
{
  "content": ["package com.denghb;", "", "" ... ],
  "runline": [3, 12, 17, ...],
  "diff": ["insert": [0, 20], "replace": [30, 34], "replace": [67, 199]]
}
```

#### /git/clone/${url}
克隆
```
ok
```

#### /git/checkout/${project}/${branch}
切换分支
```
ok
```

#### /git/pull/${project}
拉取
```
ok
```


### git clone 
org.eclipse.jgit.api.errors.TransportException: git@xxx Auth fail -> [Help 1]

`vim ~/.ssh/config` file to this
```
Host *
UserKnownHostsFile  ~/.ssh/known_hosts  
IdentityFile ~/.ssh/id_ecdsa  
HashKnownHosts no
StrictHostKeyChecking no
```

org.eclipse.jgit.api.errors.TransportException:invalid privatekey:
```
ssh-keygen -t ecdsa -b 256 -m PEM
```
re add sshkey



