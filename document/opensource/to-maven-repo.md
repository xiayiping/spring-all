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

now it's changed to https://central.sonatype.org/publish-ea/publish-ea-guide/#component-validation
https://central.sonatype.org/publish-ea/publish-ea-guide/
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


# Advertise Project 

Advertising your public project on GitHub effectively requires a combination of showcasing its value, engaging with the developer community, and leveraging multiple platforms. Below are actionable strategies you can follow to promote your GitHub project:

---

## **1. Optimize Your GitHub Repository**
Before you advertise your project, make sure your repository is polished and ready for new visitors:

### **1.1. Write a Clear and Compelling README**
The README file is the first thing users see, so make it attractive and informative. Include:
- **What the project does**.
- **Why it’s useful**.
- **How to install and use it** (with examples).
- **Screenshots, GIFs, or demos** (if applicable).
- **Contribution guidelines** to encourage collaboration.

### **1.2. Add a License**
Choose an open-source license that matches your goals (e.g., MIT, Apache 2.0). A license makes it clear others can use and contribute to your project.

### **1.3. Use a Descriptive Repository Name**
Make sure the repository name reflects the purpose of your project. Include relevant keywords to make it discoverable.

### **1.4. Add Tags and Topics**
Use GitHub topics to tag your repository with keywords related to your project. For example:
- If your project is a JavaScript library, you might tag it with `javascript`, `library`, `web-development`, etc.
- Topics help users discover your project when searching on GitHub.

### **1.5. Include Badges**
Add badges in your README (e.g., build status, downloads, license, etc.) to make your project look more professional. Use services like [Shields.io](https://shields.io) to generate badges.

---

## **2. Leverage Social Media**
Social media is a powerful tool to share your work with a wider audience.

### **2.1. Share on Twitter, LinkedIn, and Reddit**
- Use platforms like **Twitter/X**, **LinkedIn**, or developer-specific subreddits (e.g., [r/programming](https://www.reddit.com/r/programming) or [r/webdev](https://www.reddit.com/r/webdev)) to announce your project.
- Include a short description of your project, a link to the repository, and a quick demo (e.g., GIF or video).
- Use hashtags like `#opensource`, `#programming`, or tags specific to your project’s language (e.g., `#python`, `#javascript`).

### **2.2. Post on Developer Communities**
- Share your project on platforms like **Dev.to**, **Hashnode**, and **Medium**. Write a blog post explaining the problem your project solves and how it works.
- Join forums like **Hacker News** or **Stack Overflow** and share your project when it’s relevant.

---

## **3. Submit to Open Source Platforms**
- Submit your project to websites that showcase open-source projects:
   - [Product Hunt](https://www.producthunt.com): Great for launching innovative tools or products.
   - [Awesome Lists](https://github.com/sindresorhus/awesome): Find a relevant list (e.g., `awesome-javascript`) and suggest adding your project.
   - [Open Source Agenda](https://opensourceagenda.com): A platform for promoting open-source projects.

---

## **4. Engage with the Developer Community**
Building relationships within the developer community can help promote your project organically.

### **4.1. Participate in Open Source Events**
- Take part in **Hacktoberfest** or other open-source events to get contributors to notice your project.
- Present your project at meetups or conferences (in-person or virtual).

### **4.2. Collaborate with Other Projects**
- Find related projects on GitHub and contribute to them. Mention your project (when relevant) to draw attention.

### **4.3. Respond to Issues and Pull Requests**
- Be active in maintaining your repository. Respond to issues promptly and welcome contributions.
- A well-maintained project is much more attractive to potential users and contributors.

---

## **5. Create Tutorials and Videos**
People are more likely to use your project if they understand how it works.

### **5.1. Write Tutorials**
- Publish step-by-step tutorials on platforms like **Medium**, **Dev.to**, or your own blog.
- Focus on solving specific use cases with your project.

### **5.2. Create Video Tutorials**
- Record screencasts or demos of your project and upload them to **YouTube** or **Twitch**. Explain how to set it up and demonstrate its key features.

---

## **6. Use GitHub Pages**
If your project has a frontend (e.g., a web-based tool or library), host a live demo using [GitHub Pages](https://pages.github.com). A live demo makes it easier for people to see the value of your project.

---

## **7. Encourage Contributions**
- Add a **`CONTRIBUTING.md`** file to guide contributors on how to help with your project.
- Use the **"Good First Issue"** label to highlight beginner-friendly tasks.
- Actively welcome contributions and thank contributors for their work.

---

## **8. Show Your Project in Action**
- Create a **demo app** or **interactive example** that showcases your project’s capabilities.
- Use platforms like [CodeSandbox](https://codesandbox.io) or [Replit](https://replit.com) to provide live examples.

---

## **9. Monitor Analytics**
- Use GitHub’s built-in insights to monitor traffic to your repository.
- Track stars, forks, and contributions to understand how well your project is being received.

---

## **10. Consistently Update and Improve**
- Regularly update your repository with new features, bug fixes, and documentation improvements.
- Active projects are more likely to gain attention compared to abandoned ones.

---

By following these steps, you can effectively advertise your GitHub project, attract users, and build a community around it.