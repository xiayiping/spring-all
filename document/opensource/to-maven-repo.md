# How

要将代码开源并上传到 Maven Central，通常需要遵循以下几个步骤：

## 一、将代码开源到 GitHub

1. **创建 GitHub 仓库**：
    - 前往 [GitHub](https://github.com) 并登录。
    - 点击页面右上角的加号（“+”），选择“新建仓库”（New repository）。
    - 填写仓库名称，选择“公开”（Public），并填写描述。
    - 如果需要，可以选择初始化仓库时添加 `README.md` 文件。
    - 点击“创建仓库”（Create repository）。

2. **将代码推送到 GitHub 仓库**：
    - 如果你已经在本地有代码，可以通过 Git 命令将代码推送到你刚创建的 GitHub 仓库。
    - 打开终端或命令行，导航到你的项目目录，并运行以下命令：

   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin https://github.com/你的用户名/你的仓库名.git
   git push -u origin main
   ```

   这样，你的代码就会被推送到 GitHub。

## 二、准备上传到 Maven Central

上传代码到 Maven Central 需要一些额外的步骤，包括项目配置和签名验证。官方发行 Maven Central 包需要通过 [Sonatype OSSRH](https://central.sonatype.org/)（OSS Repository Hosting）。以下是详细步骤：

### 1. **创建 GPG 密钥**

Maven Central 要求你对构建的包进行签名。首先需要生成 GPG 密钥。

```bash
gpg --gen-key
```

根据提示生成密钥。生成完成后，可以通过以下命令查看密钥的详细信息：

```bash
gpg --list-keys
```

找到生成的密钥 ID（通常是一个 8 位或 16 位的字符串），并将其上传到公共密钥服务器：

```bash
gpg --keyserver keyserver.ubuntu.com --send-keys <Your-Key-ID>
```

### 2. **申请 Sonatype 账户**

- 注册并申请 OSSRH 账户：[Sonatype JIRA](https://issues.sonatype.org/secure/Signup!default.jspa)。
- 申请时，填写你在 GitHub 上的项目详细信息。
- 提交请求后，Sonatype 会审核你的申请并给你分配一个 GroupId（通常是以 `com.yourproject` 为前缀的 Java 包命名空间）。

### 3. **配置 `pom.xml`**

确保你的项目 `pom.xml` 文件符合 Maven Central 的要求，特别是以下几个字段需要正确配置：

```xml
<groupId>com.yourproject</groupId>
<artifactId>your-artifact</artifactId>
<version>1.0.0</version>

<name>Your Project Name</name>
<description>A brief description of your project</description>
<url>https://github.com/yourusername/yourrepository</url>

<licenses>
    <license>
        <name>The Apache License, Version 2.0</name>
        <url>http://www.apache.org/licenses/LICENSE-2.0.txt</url>
        <distribution>repo</distribution>
    </license>
</licenses>

<developers>
    <developer>
        <id>yourusername</id>
        <name>Your Name</name>
        <email>your.email@example.com</email>
    </developer>
</developers>

<scm>
    <connection>scm:git:git://github.com/yourusername/yourrepository.git</connection>
    <developerConnection>scm:git:ssh://github.com:yourusername/yourrepository.git</developerConnection>
    <url>https://github.com/yourusername/yourrepository</url>
</scm>
```

### 4. **配置 `distributionManagement`**

配置 Maven 的 `distributionManagement`，以便将包上传到 Sonatype 的 Nexus Repository：

```xml
<distributionManagement>
    <snapshotRepository>
        <id>ossrh</id>
        <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
    </snapshotRepository>
    <repository>
        <id>ossrh</id>
        <url>https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/</url>
    </repository>
</distributionManagement>
```

### 5. **配置签名插件**

Maven Central 要求发布的 Artifact 需要进行签名，可以在 `pom.xml` 中添加 Maven GPG 插件：

```xml
<build>
    <plugins>
        <!-- Maven GPG Plugin to sign artifacts -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-gpg-plugin</artifactId>
            <version>1.6</version>
            <executions>
                <execution>
                    <id>sign-artifacts</id>
                    <phase>verify</phase>
                    <goals>
                        <goal>sign</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <!-- Maven Source Plugin to include source code -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-source-plugin</artifactId>
            <version>3.2.1</version>
            <executions>
                <execution>
                    <id>attach-sources</id>
                    <goals>
                        <goal>jar</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>

        <!-- Maven Javadoc Plugin to include Javadocs -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-javadoc-plugin</artifactId>
            <version>3.3.0</version>
            <executions>
                <execution>
                    <id>attach-javadocs</id>
                    <goals>
                        <goal>jar</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### 6. **设置 Maven Central 的凭据**

在你的 Maven `settings.xml` 文件中（通常位于 `~/.m2/settings.xml`），添加 Sonatype OSSRH 的用户名和密码：

```xml
<servers>
    <server>
        <id>ossrh</id>
        <username>your-sonatype-username</username>
        <password>your-sonatype-password</password>
    </server>
</servers>
```

### 7. **发布流程**

1. **安装并验证 GPG 签名**：

    ```bash
    mvn clean install
    ```

2. **发布到 Sonatype Nexus**：

    ```bash
    mvn clean deploy
    ```

3. **进行 Staging 和发布**：
    - 登录 [Sonatype Nexus](https://s01.oss.sonatype.org/#stagingRepositories)。
    - 找到你刚刚上传的包，进行“关闭”（Close）操作。
    - 如果没有问题，点击“发布”（Release）按钮，包就会发布到 Maven Central。

### 8. **同步到 Maven Central**

发布后的包会经过 Sonatype 的自动同步，通常需要几个小时到一天时间。

## 参考资料

- [Sonatype OSSRH 指南](https://central.sonatype.org/publish/publish-guide/)
- [Maven GPG 插件文档](https://maven.apache.org/plugins/maven-gpg-plugin/)
- [GitHub 官方指南](https://guides.github.com/activities/hello-world/)

通过以上步骤，你就可以将代码开源到 GitHub 并成功发布到 Maven Central。