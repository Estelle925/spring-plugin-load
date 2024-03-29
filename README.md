#   基于热插拔和SPI编程思想实现动态Jar包插件加载工程，可以动态加载spring bean，注册mvc servlet路径，支持nacos-dubbo动态消费服务注册，将其打成二方包依赖到主工程，实现系统功能增强，使用场景如多通信协议驱动扩展动态加载，动态数据加工等。
### 切面类无法动态加载 !!!!!

## 分支说明：
    master分支： 支持bean、mvc动态注册管理
    base-nacos分支： 支持bean、mvc、nacos动态注册管理

## 原理：
#### 1. 自定义 PluginClassLoader 开辟出自己的classloader类，父类无法管理子类的类加载
#### 2. 自定义 PluginApplicationContext 从spring 容器中开辟出自定义管理容器空间，基于继承原理 父Application Context 无法管理子类容器空间，但是生命周期互不影响，内存互不占用。


## 使用示例

#### 1. springboot application类上添加注解

    @EnablePluginLoadServer()

#### 3. yml 配置文件上添加插件加载配置

      plugin:
        loadPath: /home/plugins
        enableSystemScan: true

*   loadPath：插件jar包保存地址，工程启动扫描地址，jar包保存地址

*   enableSystemScan：是否启用 
    

注意：插件加载配置必填，如不填工程无法启动扫描。

#### 4. 接口相关

插件加载管理入口类：PluginService

引入插件与加载平台交互api包依赖 https://github.com/Estelle925/spring-plugin-load-api
![img.png](img.png)
1. 预加入插件，返回插件相关信息如插件版本、插件参数等

```java
    /**
     * 预加载插件信息
     * @param jarPath jar包路径
     * @return PluginConfigVO
     */
    public PluginConfigVO preLoad(Path jarPath) {
        return pluginLoader.preLoad(jarPath);
    }
 ```   

2.  加载注册插件

```java
/**
 * 指定插件名和版本加载注册插件
 * @param jarPath jar包路径
 * @param pluginName 插件名
 * @param pluginVersion 插件版本
 * @return 插件加载注册成功
 */
public boolean loadAndRegister(Path jarPath, String pluginName, String pluginVersion) {
    
}
```

```java
       
    /**
     * 不指定插件名字和版本，加载注册插件
     * @param jarPath jar包路径
     * @return 插件加载注册成功
     */
    public boolean loadAndRegister(Path jarPath) {
    
    }
 ```   
3. 删除卸载插件
```java
   /**
    * 卸载删除插件
    * @param pluginName 插件名
    * @param pluginVersion 插件版本
    * @return 卸载删除成功
    */
   public boolean removeAndDestroy(String pluginName, String pluginVersion) {
   
   }
```
4.  查询所有已装配上的插件

```java
    /**
     * 获取所有插件
     * @return List<PluginConfigVO>
     */
    public List<PluginConfigVO> queryAllPlugin() {
        
    }
```
#### 5. 测试插件jar包开发，可以参考插件demo  https://github.com/Estelle925/spring-plugin-demo
##### 引入插件api依赖
![img.png](img.png)

##### 创建插件定义config类，标识插件信息
![img_1.png](img_1.png)

##### 定义demo插件controller测试
![img_2.png](img_2.png)
