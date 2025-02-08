# 🚀 Spring Plugin Load - 强大的动态插件加载框架

> 一个基于Spring生态的动态插件加载框架，让你的应用拥有无限扩展可能！

## ✨ 特性

- 🔌 **热插拔**: 运行时动态加载/卸载插件，无需重启应用
- 🎯 **Spring集成**: 完美支持Spring Bean动态注册
- 🌐 **MVC支持**: 动态注册Servlet路径
- ☁️ **微服务友好**: 支持Nacos-Dubbo服务动态注册
- 🔒 **安全隔离**: 自定义ClassLoader确保插件间隔离
- 🎨 **轻量灵活**: 作为二方包集成，即插即用

## 🎯 应用场景

- 多通信协议驱动的动态扩展
- 实时数据处理插件
- 业务功能动态扩展
- 第三方集成模块
- 定制化功能按需加载

## 🛠️ 核心原理

### ClassLoader隔离
自定义`PluginClassLoader`创建独立的类加载空间，确保插件间的完全隔离。

### Spring容器管理
定制`PluginApplicationContext`实现插件容器的自主管理，做到:
- 生命周期独立管理
- 内存空间互不影响
- 上下文完全隔离

## 📦 快速开始

### 1. 启用插件加载

在主应用类上添加注解：
```java
@EnablePluginLoadServer
```

### 2. 配置插件信息

在`application.yml`中添加：
```yaml
plugin:
  loadPath: /home/plugins     # 插件jar包扫描路径
  enableSystemScan: true      # 启用系统扫描
```

### 3. API使用指南

核心接口类：`PluginService`

#### 插件预加载
```java
PluginConfigVO config = pluginService.preLoad(jarPath);
```

#### 加载并注册插件
```java
// 指定版本加载
boolean success = pluginService.loadAndRegister(jarPath, pluginName, version);

// 快速加载
boolean success = pluginService.loadAndRegister(jarPath);
```

#### 卸载插件
```java
boolean success = pluginService.removeAndDestroy(pluginName, version);
```

#### 查询已加载插件
```java
List<PluginConfigVO> plugins = pluginService.queryAllPlugin();
```

## 📚 相关资源

- [插件开发API](https://github.com/Estelle925/spring-plugin-load-api)
- [插件开发Demo](https://github.com/Estelle925/spring-plugin-demo)

## ⚠️ 注意事项

- 插件配置为必填项，否则系统无法启动扫描
- 目前暂不支持切面类的动态加载
- 建议在开发插件时严格遵循示例规范

## 📄 开发插件示例

1. 引入插件API依赖
2. 创建插件配置类定义插件信息
3. 开发业务功能（Controller、Service等）

详细示例请参考[插件开发Demo](https://github.com/Estelle925/spring-plugin-demo)

## 🤝 贡献

欢迎提交Issue和Pull Request！

---

如果这个项目对你有帮助，请给个 ⭐️ 鼓励一下~
