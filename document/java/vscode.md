# formatter

在 VSCode 的 Java 扩展中，想要**完全保留你手动添加的所有换行**，同时让格式化工具**只负责调整缩进和空格**，你需要修改两个核心设置：

1.  **关闭"合并行"功能**：告诉格式化工具不要把你手动换行的代码重新拼回去。
2.  **设置"永不合并行"策略**：确保工具在任何情况下都不会添加或删除换行符。

下面提供两种方法，推荐使用**方法一 (UI 预览编辑器)**，它更直观，可以实时看到效果。

---

### 🔧 方法一：使用 UI 预览编辑器 (推荐)

VSCode 的 Java 扩展提供了一个图形化编辑器，可以方便地调整格式化配置并实时预览。

1.  按下 `Ctrl+Shift+P` (Mac 上是 `Cmd+Shift+P`)，打开命令面板。
2.  输入并选择 **`Java: Open Java Formatter Settings with Preview`**。
    -   如果这是你第一次操作，VSCode 会在项目根目录的 `.vscode` 文件夹下自动生成一个名为 `java-formatter.xml` 的配置文件。
3.  在打开的编辑器中，切换到 **`Line Wrapping`** 标签页。
4.  找到 **`General settings`** 区域，将 **`Never join already wrapped lines`** 选项**勾选**上。这个选项的作用是告诉格式化工具：已经换行的代码，不要再给我合并回去。
5.  建议将 **`Maximum line width`** (最大行宽) 设置为一个较大的值（例如 `200` 或 `999`），这样可以有效避免因行宽限制而自动产生你预期外的换行。
6.  点击右上角的 **`Save`** 按钮，你的配置文件就会自动更新，之后格式化就会遵循新的规则。

---

### ⚙️ 方法二：直接修改配置文件 (XML)

如果你习惯直接修改代码，可以手动编辑 `.vscode/java-formatter.xml` 文件。

1.  在项目根目录的 `.vscode` 文件夹下找到 `java-formatter.xml` 文件。如果没有，可以用方法一生成一份。
2.  在 `<profile>` 标签内，找到或添加以下 `<setting>` 条目：

    ```xml
    <!-- 核心设置：禁止将已经换行的行合并（解决你的主要问题） -->
    <setting id="org.eclipse.jdt.core.formatter.join_wrapped_lines" value="false"/>

    <!-- 建议设置：将最大行宽设为一个较大的值，避免因行宽限制而自动换行 -->
    <setting id="org.eclipse.jdt.core.formatter.lineSplit" value="200"/>

    <!-- 可选设置：根据你的编码规范，配置缩进是空格还是 Tab -->
    <!-- value="space" 表示使用空格；value="tab" 表示使用制表符 -->
    <setting id="org.eclipse.jdt.core.formatter.tabulation.char" value="space"/>
    <!-- 缩进大小，例如 4 个空格 -->
    <setting id="org.eclipse.jdt.core.formatter.indentation.size" value="4"/>
    ```
3.  保存文件，之后对 Java 文件执行格式化，就会应用此配置。

---

### 💡 关键说明

*   **核心原理**：Java 扩展的底层是 Eclipse 的格式化器，`join_wrapped_lines` 这个参数就是控制换行行为的“总开关”。将其设为 `false` 后，格式化工具将只关注你要求的缩进和空格调整，而会尊重你代码中的原有换行。
*   **特殊场景**：对于 `if` 等单行语句，如果想让其保持在一行而不是被强制换行，可以额外添加以下配置：
    ```xml
    <setting id="org.eclipse.jdt.core.formatter.keep_imple_if_on_one_line" value="true"/>
    ```
*   **完全屏蔽（备选）**：如果希望格式化工具完全跳过某一段代码，不对它做任何改动，可以使用特殊的注释将代码包裹起来：
    ```java
    // @formatter:off
    // 在这里，你可以随心所欲地排版
    someClass.someMethod( argOne,
                          argTwo,
                          argThree );
    // @formatter:on
    ```

这些配置主要影响的是手动执行 `Format Document` (快捷键 `Shift+Alt+F`) 时的行为。如果你发现保存文件或构建项目时格式也会自动变化，可以检查一下项目构建文件中是否配置了 `spotless` 等自动格式化插件。

