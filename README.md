http://localhost:2000/

v1、v3都是可以运行的版本，但是在v3上开发。

文件的读写，还没有掌握。

## 20191126

### bug

1.三个上传框，若只选择二个上传文件，服务器会出错，浏览器收不到回应。

### todo

* List

1.自动识别请求方式（GET或POST），并进行相应处理。 -- OK
	
2.转发动态请求给CGI（如PHP）处理。

	获取GET参数
	获取POST数据（表单数据：文本 + 文件）

	转发动态请求，是直接将Uri转发过去，还是需要附加上获取到的数据呢？

	不知道，尝试吧。

	转发PHP文件。

3.代码封装

已经无力再封装了，封装耗费了很多时间

20191127

1.完善和彻底理解fastcig协议

从 php 获取中文后，输出内容，乱码，如何解决？

在java中直接输出中文，不乱码。

乱码原因，读取的时候，8字节为一次读取单位，然后进行了字符串转换（可能刚好不足以转为完整的中文）。

解决措施，调整为1024 * 8字节为一次读取单位，我觉得仍然会存在问题。

2.完善POST和GET请求

有针对静态页面的POST请求吗？

先进行动态请求与静态请求的区分与处理，

再进行GET与POST请求的处理：看上一步的结果，有，返回该结果；无，返回静态资源。

fastcgi,header type == 4 时，传递的数据全部在PHP的$_SERVER中。

如何向 PHP-FPM 传递 $_FILES、$_POST 数据？

fastcig 协议

https://fastcgi-archives.github.io/FastCGI_Specification.html

web服务器是把从浏览器接收到的数据，全部原样交给php-fpm，由php-fpm去解析entitybody吗？

基本可以确定，是的。

/Users/cg/data/www/cg/tool/tmp.php:65:
array (size=1)
  'picture' =>
    array (size=5)
      'name' => string 'tooopen_sy_104114411474007.jpg' (length=30)
      'type' => string '' (length=0)
      'tmp_name' => string '' (length=0)
      'error' => int 3
      'size' => int 0

上传图片，tmp_name 和 size 还缺乏，需要web服务器深入到 entity 去补充这些数据吗？

PHP 文件不存在时，php-fpm返回下面的数据
Primary script unknown
 . k Status: 404 Not Found
X-Powered-By: PHP/7.2.12
Content-type: text/html; charset=UTF-8

File not found.

已经完善。

PHP 404，网页不正常，此时调试效率已经非常低。

20191128

1.反向代理

location / {
               proxy_pass http://myServer;     # 注意这里的写法，必须加上 http
              #root   /Users/cg/data/www/code;
              #index  index.html index.htm;
          }

upstream myServer{
          server 127.0.0.1:2000; # 注意这里的写法，不能加上 http
          server 127.0.0.1:2000;
  }

本服务器接收 HTTP 请求报文后，将其转发给目标服务器，然后将数据返回给客户端。

1.接收全部数据后，再转发

2.一边接收数据，一边转发

采用第二种。此种思路实现反向代理（不包括负载均衡），通过验证了，还有细节需要完善。

3.如何根据配置文件来选择是代理还是非代理？

1>仿照nginx的配置文件---》自己解析

此技能，在工作中用处小

2>使用json----》有开源解析库

熟悉新工具，纯体力活

用这个。毕竟需要用java干活

引入 fastjson

-------------------------
idea 对module 配置信息之意， infomation of module
iml是 intellij idea的工程配置文件，里面是当前project的一些配置信息。.idea存放项目的配置信息，包括历史记录，版本控制信息等。

-------------------------

-------------------------
Maven

https://www.runoob.com/maven/maven-pom.html

在 /Users/cg/data/code/wheel/java/demo 创建文件 pom.xml，写入内容

<project xmlns = "http://maven.apache.org/POM/4.0.0"
         xmlns:xsi = "http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation = "http://maven.apache.org/POM/4.0.0
    http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <!-- 模型版本 -->
    <!-- 最小必须是4.0.0--->
    <modelVersion>4.0.0</modelVersion>
    <!-- 公司或者组织的唯一标志，并且配置时生成的路径也是由此生成， 如com.companyname.project-group，maven会将该项目打成的jar包放本地路径：/com/companyname/project-group -->
    <groupId>com.companyname.project-group</groupId>

    <!-- 项目的唯一ID，一个groupId下面可能多个项目，就是靠artifactId来区分的 -->
    <artifactId>my-web-server</artifactId>

    <!-- 版本号 -->
    <version>1.0</version>
</project>

执行 mvn help:effective-pom

新增

<dependencies>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.9</version>
        </dependency>
    </dependencies>


执行命令

## mvn install ## 构建了 jar 包 ，在

Copying fastjson-1.2.9.jar to /Users/cg/data/code/wheel/java/demo/target/dependency/fastjson-1.2.9.jar

需要将依赖下载到本地吗？

mvn -f pom.xml dependency:copy-dependencies

执行mvn 报错 source-1.5 中不支持 diamond运算符
指定Maven的版本，并且指定Maven使用的jdk版本

在pom.xml中修改

<project xmlns="...">
    ...
    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>3.3</version>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
        </plugins>
    </build>
    ...
</project>


HashMap的value和key能使用泛型吗？有时value不一致

使用 fastjson 花了大概1.5小时

接下来，根据配置文件，创建多个服务器，即，多虚拟机功能

nginx 的 master 进程没有监听 tcp，很意外。

nginx 的一个work进程，监听了多个端口的tcp。

使用多线程，实现多虚拟机功能

代理超时，修改 bug 花了一些时间，之前的硬编码数据未及时修改，再加上具备变量和类field同名

long型转String

long a1 = 12;
        String s1 = a1 + "";                  // 法1：直接加空串
        System.out.println(s1 + 999);

        long a2 = 34;
        String s2 = String.valueOf(a2);      // 法2：String.valueOf()
        System.out.println(s2 + 999);

        long a3 = 56;
        String s3 = Long.toString(a3);       // 法3：Long.toString()
        System.out.println(s3 + 999);




-------------------------

4.后续功能点

cookie

session

error_log

access_log

支持显示列表文件

代理超时，即Java 的socket 超时，未验证

性能很差，代理，连续两个404请求，第二个请求pending很久才会获取到结果

类似 nginx 的 显示服务器目录功能

调试 目录中文件的路径，耗费了一些时间。这种细小的问题，已经和web服务器没啥关系了，在任何开发工作中，都有可能遇到。


20191129

格式化时间

lastModified

measured in milliseconds since the epoch

格式化日期格式

https://www.cnblogs.com/cx-zyq/p/7771359.html

命令行忽略文件

创建 .gitignore文件，在文件中写入要忽略的文件

access_log，大量的重复代码，做了很多复制粘贴工作，解决IDEA，将重复代码封装成了方法。

access_log 中的 内容长度 是请求发送的还是返回数据的？


[WARNING]
[WARNING] Some problems were encountered while building the effective model for com.companyname.project-group:my-web-server:jar:1.0
[WARNING] 'build.plugins.plugin.version' for org.apache.maven.plugins:maven-compiler-plugin is missing. @ line 19, column 21
[WARNING]
[WARNING] It is highly recommended to fix these problems because they threaten the stability of your build.
[WARNING]
[WARNING] For this reason, future Maven versions might no longer support building such malformed projects.

[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!

[WARNING] JAR will be empty - no content was marked for inclusion!

[WARNING] Configuration options: 'appendAssemblyId' is set to false, and 'classifier' is missing.
Instead of attaching the assembly file: /Users/cg/data/code/wheel/java/demo/target/my-web-server-1.0.jar, it will become the file for main project artifact.
NOTE: If multiple descriptors or descriptor-formats are provided for this project, the value of this file will be non-deterministic!
[WARNING] Replacing pre-existing project main-artifact file: /Users/cg/data/code/wheel/java/demo/target/my-web-server-1.0.jar
with assembly file: /Users/cg/data/code/wheel/java/demo/target/my-web-server-1.0.jar

chugangdeMacBook-Pro:target cg$ java -jar my-web-server-1.0.jar
shell-init: error retrieving current directory: getcwd: cannot access parent directories: No such file or directory
shell-init: error retrieving current directory: getcwd: cannot access parent directories: No such file or directory
pwd: error retrieving current directory: getcwd: cannot access parent directories: No such file or directory

旧 pom 文件内容

<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
    http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <!-- 模型版本 -->
    <modelVersion>4.0.0</modelVersion>
    <!-- 公司或者组织的唯一标志，并且配置时生成的路径也是由此生成， 如com.companyname.project-group，maven会将该项目打成的jar包放本地路径：/com/companyname/project-group -->
    <groupId>com.companyname.project-group</groupId>

    <!-- 项目的唯一ID，一个groupId下面可能多个项目，就是靠artifactId来区分的 -->
    <artifactId>my-web-server</artifactId>

    <!-- 版本号 -->
    <version>1.0</version>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>1.8</source>
                    <target>1.8</target>
                </configuration>
            </plugin>
            <!-- 可执行jar 插件  -->
            <plugin>
                <artifactId>maven-assembly-plugin</artifactId>
                <configuration>
                    <appendAssemblyId>false</appendAssemblyId>
                    <descriptorRefs>
                        <descriptorRef>jar-with-dependencies</descriptorRef>
                    </descriptorRefs>
                    <archive>
                        <manifest>
                            <!-- 此处指定main方法入口的class -->
                            <mainClass>Server</mainClass>
                        </manifest>
                    </archive>
                </configuration>
                <executions>
                    <execution>
                        <id>make-assembly</id>
                        <phase>package</phase>
                        <goals>
                            <goal>assembly</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>fastjson</artifactId>
            <version>1.2.9</version>
        </dependency>
    </dependencies>

</project>

新


chugangdeMacBook-Pro:target cg$ java -jar original-cg-web-server-1.0-SNAPSHOT.jar
original-cg-web-server-1.0-SNAPSHOT.jar中没有主清单属性


[ERROR] Failed to execute goal org.apache.maven.plugins:maven-compiler-plugin:3.1:compile (default-compile) on project my-web-server: Compilation failure: Compilation failure:
[ERROR] /Users/cg/data/code/wheel/java/demo/src/main/java/com/company/ConfigParserTest.java:[3,29] 程序包org.junit.jupiter.api不存在

解决方法：

按 这个 <sourceFolder url="file://$MODULE_DIR$/test" type="java-test-resource" />
           <sourceFolder url="file://$MODULE_DIR$/src/main/java" isTestSource="false" />
           <excludeFolder url="file://$MODULE_DIR$/target" />
调整了 test 文件夹位置

[WARNING] Using platform encoding (UTF-8 actually) to copy filtered resources, i.e. build is platform dependent!

解决：

<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

===============

[WARNING] Configuration options: 'appendAssemblyId' is set to false, and 'classifier' is missing.
Instead of attaching the assembly file: /Users/cg/data/code/wheel/java/demo/target/my-web-server-1.0.jar, it will become the file for main project artifact.
NOTE: If multiple descriptors or descriptor-formats are provided for this project, the value of this file will be non-deterministic!
[WARNING] Replacing pre-existing project main-artifact file: /Users/cg/data/code/wheel/java/demo/target/my-web-server-1.0.jar
with assembly file: /Users/cg/data/code/wheel/java/demo/target/my-web-server-1.0.jar


错误

./com/company/ConfigParser.java:5: 错误: 程序包com.alibaba.fastjson不存在

20191130

1.多线程和线程池使用

工作线程，使用线程池。第二个http请求，会使用第一个http请求建立的socket吗？

不会，因为工作线程使用的socket是浏览器新建之后用参数传入的。

错误

'build.plugins.plugin.version' for org.apache.maven.plugins:maven-compiler-plugin is missing.

解决

在 <plugin>
                  <groupId>org.apache.maven.plugins</groupId>
                  <artifactId>maven-compiler-plugin</artifactId>
                  <version>3.3.9</version>
                  <configuration>
                      <source>1.8</source>
                      <target>1.8</target>
                  </configuration>
              </plugin>
加入   <version>3.3.9</version> 后，仍然不能解决问题，但出现下载动作（太慢，未执行）
