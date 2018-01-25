# 本文链接

https://www.jianshu.com/p/32c150f0cb20

# 本文环境基础

- Gradle:4.1

- AndroidGradleTools:3.0.1

- AndroidStudio:3.0.1

# Talk is cheap. Show me the code.

https://github.com/sandy1108/MyTestGradlePluginProject

# 工程配置

1. 在AndroidStudio中，新建一个Android工程。

2. 新工程中, 可以再创建一个Android的Module。核心内容如下：
```
-project
    -libs
    -src
        -androidTest
        -main
            -java
            -res
            -AndroidManifest.xml
        -test
    -build.gradle
```

3. 我们改为这样：
```
-project
    -libs
    -src
        -main
            -java
            -groovy
                -org
                    -wsgh
                        MyGradlePlugin.groovy
            -resources
                -META-INF
                    -gradle-plugins
                        mygradleplugin.properties
    -build.gradle
```
其中，mygradleplugin.properties的文件名就是你的gradle插件名成，也就是最后你在gradle中apply plugin指定的那个插件名。而此文件内的内容如下：

```
implementation-class = org.wsgh.MyGradlePlugin
```
这里面指定的就是你的Gradle插件的入口类。然后你就可以在groovy目录中对应的按照包路径新建一个以此命名的groovy文件。作为入口类：

4. build.gradle改为：
```
apply plugin: 'groovy'

dependencies {
    compile gradleApi()
    compile localGroovy()
}
```

5. MyGradlePlugin.groovy中：

```
public class MyGradlePlugin implements Plugin<Project>{
    @Override
    void apply(Project project) {
        //TODO gradle中apply这个插件时，会执行这个回调方法，我们可以在这里处理一些逻辑，进行插件的初始化操作。
        println("Hello Groovy World！！！")
    }
}
```

6. 如果为了调试方便，可以修改步骤2中新建的module的名字，为buildSrc。这样，在整个project的gradle文件中，就可以直接apply你这个plugin，查看效果了。至此，基本工程配置就算完成了。

# Gradle插件基本内容：Task

## Task理解

一个Task包含若干Action。所以，Task有doFirst和doLast等函数，用于添加需要最先执行的Action和需要和需要最后执行的Action。Action就是一个闭包。闭包，英文叫Closure，是Groovy中非常重要的一个数据类型或者说一种概念。

## Task创建

1. 普通方式

```
task myTask
task myTask { configure closure }  // closure是一个闭包，代表任务内容

```

2. “继承”方式

Type可以指定任务类型，实际上就类似于继承的关系，创建了一个任务的子类任务。Gradle本身提供有Copy、Delete、Sync等，你也可以继承你自己定义的Task。

```
task myTask(type: SomeType)
task myTask(type: SomeType) { configure closure }
```

3. 过时方式（据说Gradle5.0就要删了）

<<符号是doLast的缩写，现在已经过时，推荐直接用doLast

```
task myType << { task action }
```

4. Task“动态”创建

其实这里说动态创建，只是我自己这么称呼的，因为这是可以用字符串作为task名称的方法，是一种看起来更灵活一点的创建方式。

```
//project为当前工程的project对象，在gradle脚本中可以直接用，若是groovy的gradle插件的话，在实现apply方法时，会回调来一个project对象。

project.tasks.create("YourTaskName")
```

## Task执行顺序变换

1. doLast和doFirst：将Action追加到Task最后面（最前面）。

代码举例：
```
        def task = project.tasks.create("task")
        task.doLast {
            println("===============>task, Last1")
        }
        task.doLast {
            println("===============>task, Last2")
        }
        task.doFirst {
            println("===============>task, First1")
        }
        task.doFirst {
            println("===============>task, First2")
        }
```
输出结果应该是：
```
===============>task, First2
===============>task, First1
===============>task, Last1
===============>task, Last2
```

2. dependsOn：声明本Task在执行之前必须先执行完成dependsOn后面指定的Task，可以指定多个Task为依赖，顺序不明，据说貌似是按照Task名称排序，但这不重要。

代码举例：

```
        def task2 = project.tasks.create("task2")
        task2.doLast {
            println("===============>task2")
        }
        def task1 = project.tasks.create("task1")
        task1.doLast {
            println("===============>task1")
        }
        task1.dependsOn(task2)
```

这时，我们直接执行task1，无需执行task2。输出结果应该是：

```
===============>task2
===============>task1
```


3. finalizedBy：声明本Task在执行之后必须要继续执行完成finalizedBy后面指定的Task，可以指定多个Task为结尾，顺序同上。

代码举例：

```
        def task2 = project.tasks.create("task2")
        task2.doLast {
            println("===============>task2")
        }
        def task1 = project.tasks.create("task1")
        task1.doLast {
            println("===============>task1")
        }
        task1.finalizedBy(task2)
```

这时，我们直接执行task1，无需执行task2。输出结果应该是：

```
===============>task1
===============>task2
```


4. mustRunAfter：为啥dependsOn和finalizedBy不能规定顺序呢？没事，这个mustRunAfter就可以解决。可以规定当多个无序任务在执行时，谁必须在谁后面执行。

代码举例：

```
        def task2 = project.tasks.create("task2")
        task2.doLast {
            println("===============>task2")
        }
        def task1 = project.tasks.create("task1")
        task1.doLast {
            println("===============>task1")
        }
        def task3 = project.tasks.create("task3")
        task3.doLast {
            println("===============>task3")
        }
        def task = project.tasks.create("testMain")
        task.doLast {
            println("===============>taskMainLastDo")
        }
        task.doFirst {
            println("===============>taskMainFirstDo")
        }
        task.dependsOn(task1,task2,task3)
        task2.mustRunAfter(task3)
```

直接执行task任务，无需执行task1，task2，task3。输出结果应该为：

```
:task1
===============>task1
:task3
===============>task3
:task2
===============>task2
:testMain
===============>taskMainFirstDo
===============>taskMainLastDo
```

5. 以上这几种排序的方式基本解决了我的一些常用需求，如果还有更好的方式，欢迎大神指点！

# 发布在本地Maven

```
apply plugin: 'maven'

group='org.wsgh.gradle.plugins'
version='1.0.0'
def artifactName='my-gradle-plugin'

uploadArchives {
    repositories {
        mavenDeployer {
            repository(url: uri('/Users/zhangyipeng/Maven')){
                //自定义maven仓库最后的artifactId，也就是版本号前面的那部分名字。不定义的话，默认会采用module名
                pom.artifactId = artifactName
            }
        }
    }
}
```

# 从Maven引用

```
buildscript {
    repositories {
        maven {
            url uri('/Users/zhangyipeng/Maven')
        }
    }
    dependencies {
        classpath "org.wsgh.gradle.plugins:my-gradle-plugin:1.0.0"
    }
}
```

# 执行和测试Task

1. AndroidStudio右边有个Gradle标签，点击会展开一个列表，展开你已经apply了plugin的那个module（或者我是在root project测试的），去other中找，就看到你定义的task了，双击即可执行。

2. 另一种方法，点开AndroidStudio下面的Terminal，应该是直接在当前工程目录的，直接执行。比如我要执行testMain任务:
    ```
    ./gradlew testMain
    
    //如果是Mac系统，可能会报没有权限的错误，可以执行下面代码加权限
    chmod +x gradlew
    ```

# 应用场景举例（待完善）

想整理一些例子，不过感觉意义不是特别大。今后慢慢完善吧。

## 版本号自增长

工作中需要自动化发布一个SDK包，现在最后想要每次递增版本号，如何实现呢？想了一个思路，就是写入本地配置文件，每次使用记录一下。这里用到了Properties类进行读写配置文件。在此记录：

```
    def getEngineLocalBuildVersionCode(String engineVersion) {
        def propertiesFileName = 'local-appcanengine-build-versions.properties'
        def versionPropertiesFile = new File(propertiesFileName)
        if (!versionPropertiesFile.exists()){
            println("Local BuildVersion Record file is not found，create it：${propertiesFileName}")
            versionPropertiesFile.createNewFile();
        }
        if (versionPropertiesFile.canRead()) {
            def Properties versionProps = new Properties()
            versionPropertiesFile.withInputStream {
                fileStream -> versionProps.load(fileStream)
            }
            def versionCode = 0;
            def buildVersionCodeStr = versionProps[engineVersion]
            if (buildVersionCodeStr != null) {
                versionCode = buildVersionCodeStr.toInteger()
            }
            versionProps[engineVersion] = (++versionCode).toString()
            versionProps.store(versionPropertiesFile.newWriter(), "Output AppCanEngine Increasement Local BuildVersion Record. \nIt is an AUTO-GENERATE file in AppCanEngine's compiling. Please Do NOT modify it manually.")
            if (versionCode < 10){
                buildVersionCodeStr = "0${versionCode}"
            }else{
                buildVersionCodeStr = versionCode;
            }
            println("AppCanEngine buildVersion is ${buildVersionCodeStr}")
            return buildVersionCodeStr
        } else {
            throw GradleException("Cannot find or create ${propertiesFileName}!")
        }
    }
```

# 新版本兼容问题：GradleWrapper配置为4.1版本以上，AndroidGradleTools配置为3.0.1以上

1. BaseVariantOutputData类没了，直接用父类就行了：
```
import com.android.build.gradle.internal.variant.BaseVariantOutputData
```

2. sdkHandler属性没了，之前通过这个属性来获取sdk目录ndk目录等信息，现在要更换实现方式了：
```
project.plugins.findPlugin(‘com.android.application’).sdkHandler
```
应更换其他的实现方式。举例如下：
```
project.android.sdkDirectory
```
或者：
```
def plugin = project.plugins.findPlugin('android') ?:
                project.plugins.findPlugin('android-library')

plugin.extension.sdkDirectory
```

3. repositories中，增加google()，否则一些新版的库在jcenter中找不到了。


# 学习过程参考

[Gradle for Android](https://segmentfault.com/a/1190000004229002)

[Gradle Task的使用](https://www.jianshu.com/p/cd1a78dc8346)

[如何使用Android Studio开发Gradle插件](http://blog.csdn.net/sbsujjbcy/article/details/50782830)

[官方API](https://docs.gradle.org/current/dsl/org.gradle.api.Task.html)

[使用Groovy操作文件](https://yq.aliyun.com/articles/25684)

[Gradle User Guide 中文版](http://wiki.jikexueyuan.com/project/GradleUserGuide-Wiki/)
