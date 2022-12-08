# 动态Jar包插件加载工程

## 原理：
#### 1. 自定义 PluginClassLoader 开辟出自己的classloader类，父类无法管理子类的类加载
#### 2. 自定义 PluginApplicationContext 从spring 容器中开辟出自定义管理容器空间，基于继承原理 父Application Context 无法管理子类容器空间，但是生命周期互不影响，内存互不占用。

## 分支管理说明
####  develop 分支是插件加载开发核心分支。
####  common 分支是特性分支，在develop分支上构建出特性分支的。

## 使用依赖示例
#### 1 .引入插件加载 maven 依赖二方包

    <dependency>
      <groupId>evision.iot</groupId>
      <artifactId>evision-plugin-load</artifactId>
      <version>1.0.3-SNAPSHOT</version>
    </dependency>

#### 2. springboot application类上添加注解

    @EnablePluginLoadServer()

#### 3. yml 配置文件上添加插件加载配置

    evisionos:
      plugin:
        loadPath: /Users/chenhaiming/data/tb/driver/  
        enableSystemScan: true

*   loadPath：插件jar包保存地址，工程启动扫描地址，jar包保存地址

*   enableSystemScan：是否启用 
    

注意：插件加载配置必填，如不填工程无法启动扫描。

#### 4. 接口相关

1.  插件加载管理入口类：PluginService

    1.  预加入插件，返回插件相关信息如插件版本、插件参数等


     public PluginConfigVO preLoad(Path jarPath) {
            
      }

2.  加载注册插件


       public boolean loadAndRegister(Path jarPath, String pluginName, String pluginVersion) {


3.  删除卸载插件


       public boolean removeAndDestroy(String pluginName, String pluginVersion) {
           
        }

4.  查询所有已装配上的插件


     public String queryAllPlugin() {
            
      }

#### 5. 测试插件jar包
