# maikaze
Maikaze

一个在线玩舰娘的东西，功能类似ooi，支持一个用户配置多个舰娘账号，各个账号互不影响

## 编译打包运行
```shell
mvn clean package
java -jar target/maikaze-0.0.1-SNAPSHOT.jar
```
## 配置
配置都在application.yml里，请修改完再编译打包

```yaml
// 是否使用代理，如果直接跑在日本的服务器上，那么就不需要代理了
kancolle.proxy: true
// 代理IP
kancolle.proxy-ip: localhost
// 代理端口
kancolle.proxy-port: 1080
// 代理类型，支持HTTP、HTTPS、SOCKS
kancolle.proxy-type: HTTP
```

端口默认为80，如果需要更改端口，在运行命令后面增加参数
```
java -jar target/maikaze-0.0.1-SNAPSHOT.jar --server.port=8888
```


## TODO
1. 缓存修改为@Cacheable
