# SProxy
1、Netty socks 5 代理实战项目
2、该项目的特点：完全依赖于netty 的 流式处理模型 进行加密、解密、转发、状态变化。

# 部署
1、Front 部署在本地。
   修改：SocksConf 这个类的 remoteaddr，用Sea 的真实IP。
2、Sea 部署远方，可以畅游网络。

# 畅游网络
1、建议 可以用Firefox 的代理模式，选用socks5
  IP：Front 的IP
  端口：1080
