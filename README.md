# maikaze
Maikaze

一个在线玩舰娘的东西，功能类似ooi，支持一个用户配置多个舰娘账号，各个账号互不影响

## 编译打包运行
```shell
mvn clean package
java -jar target/maikaze-0.0.1-SNAPSHOT.jar
```
## 配置
配置都在application.yml里，请修改完再编译打包，也可以添加到启动参数中

```yaml
// 端口号
server.port: 80

// 是否使用代理，如果直接跑在日本的服务器上，那么就不需要代理了
kancolle.proxy: true
// 代理IP
kancolle.proxy-ip: localhost
// 代理端口
kancolle.proxy-port: 1080
// 代理类型，支持HTTP、HTTPS、SOCKS
kancolle.proxy-type: HTTP
```


## 其他

1. POI似乎不支持https，所以poi.html里的protocol写死的是http
