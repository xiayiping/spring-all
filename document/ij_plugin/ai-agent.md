# FIrstly

I'll guide you through creating an IntelliJ plugin that integrates with your AI agent API. Here's a comprehensive approach:

## Project Setup

First, create a new IntelliJ Platform Plugin project:

1. **Create the plugin structure** using Gradle with the following `build.gradle.kts`:

```kotlin
plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.16.0"
}

group = "com.yourcompany"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
}

intellij {
    version.set("2023.2")
    type.set("IC")
    plugins.set(listOf())
}

tasks {
    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("242.*")
    }
}
```

## Core Components

### 1. Plugin Configuration (`plugin.xml`)

```xml
<idea-plugin>
    <id>com.yourcompany.ai-code-assistant</id>
    <name>AI Code Assistant</name>
    <vendor>Your Company</vendor>
    
    <description>
        AI-powered code generation and completion assistant
    </description>
    
    <depends>com.intellij.modules.platform</depends>
    
    <extensions defaultExtensionNs="com.intellij">
        <!-- Settings -->
        <applicationConfigurable
                instance="com.yourcompany.aiassistant.settings.AIAssistantConfigurable"/>
        <applicationService
                serviceImplementation="com.yourcompany.aiassistant.settings.AIAssistantSettings"/>
        
        <!-- Tool Window -->
        <toolWindow id="AI Assistant"
                    anchor="right"
                    factoryClass="com.yourcompany.aiassistant.ui.AIAssistantToolWindowFactory"
                    icon="/icons/ai_assistant.svg"/>
    </extensions>
    
    <actions>
        <action id="AIAssistant.GenerateCode"
                class="com.yourcompany.aiassistant.actions.GenerateCodeAction"
                text="Generate Code with AI"
                description="Generate code using AI agent">
            <add-to-group group-id="EditorPopupMenu" anchor="first"/>
            <keyboard-shortcut keymap="$default" first-keystroke="ctrl alt G"/>
        </action>
        
        <action id="AIAssistant.CompleteCode"
                class="com.yourcompany.aiassistant.actions.CompleteCodeAction"
                text="Complete Code with AI"
                description="Complete current code using AI">
            <add-to-group group-id="EditorPopupMenu" anchor="first"/>
            <keyboard-shortcut keymap="$default" first-keystroke="ctrl alt C"/>
        </action>
        
        <action id="AIAssistant.GenerateProject"
                class="com.yourcompany.aiassistant.actions.GenerateProjectAction"
                text="Generate Project with AI"
                description="Generate entire project structure">
            <add-to-group group-id="NewGroup" anchor="first"/>
        </action>
    </actions>
</idea-plugin>
```

### 2. Settings Storage

```kotlin
// AIAssistantSettings.kt
package com.yourcompany.aiassistant.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "AIAssistantSettings",
    storages = [Storage("AIAssistantSettings.xml")]
)
class AIAssistantSettings : PersistentStateComponent<AIAssistantSettings.State> {
    
    data class State(
        var apiKey: String = "",
        var apiEndpoint: String = "https://api.yourai.com/v1/generate",
        var maxTokens: Int = 2000,
        var temperature: Double = 0.7
    )
    
    private var state = State()
    
    override fun getState(): State = state
    
    override fun loadState(state: State) {
        this.state = state
    }
    
    companion object {
        fun getInstance(): AIAssistantSettings {
            return ApplicationManager.getApplication()
                .getService(AIAssistantSettings::class.java)
        }
    }
}
```

### 3. Settings UI

```kotlin
// AIAssistantConfigurable.kt
package com.yourcompany.aiassistant.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JPanel

class AIAssistantConfigurable : Configurable {
    private var apiKeyField: JBPasswordField? = null
    private var endpointField: JBTextField? = null
    private var maxTokensField: JBTextField? = null
    private var temperatureField: JBTextField? = null
    
    override fun getDisplayName(): String = "AI Code Assistant"
    
    override fun createComponent(): JComponent {
        apiKeyField = JBPasswordField()
        endpointField = JBTextField()
        maxTokensField = JBTextField()
        temperatureField = JBTextField()
        
        return FormBuilder.createFormBuilder()
            .addLabeledComponent(JBLabel("API Key:"), apiKeyField!!, 1, false)
            .addLabeledComponent(JBLabel("API Endpoint:"), endpointField!!, 1, false)
            .addLabeledComponent(JBLabel("Max Tokens:"), maxTokensField!!, 1, false)
            .addLabeledComponent(JBLabel("Temperature:"), temperatureField!!, 1, false)
            .addComponentFillVertically(JPanel(), 0)
            .panel
    }
    
    override fun isModified(): Boolean {
        val settings = AIAssistantSettings.getInstance().state
        return apiKeyField?.password?.concatToString() != settings.apiKey ||
               endpointField?.text != settings.apiEndpoint ||
               maxTokensField?.text != settings.maxTokens.toString() ||
               temperatureField?.text != settings.temperature.toString()
    }
    
    override fun apply() {
        val settings = AIAssistantSettings.getInstance()
        settings.state.apiKey = apiKeyField?.password?.concatToString() ?: ""
        settings.state.apiEndpoint = endpointField?.text ?: ""
        settings.state.maxTokens = maxTokensField?.text?.toIntOrNull() ?: 2000
        settings.state.temperature = temperatureField?.text?.toDoubleOrNull() ?: 0.7
    }
    
    override fun reset() {
        val settings = AIAssistantSettings.getInstance().state
        apiKeyField?.text = settings.apiKey
        endpointField?.text = settings.apiEndpoint
        maxTokensField?.text = settings.maxTokens.toString()
        temperatureField?.text = settings.temperature.toString()
    }
}
```

### 4. AI Agent Service

```kotlin
// AIAgentService.kt
package com.yourcompany.aiassistant.service

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.yourcompany.aiassistant.settings.AIAssistantSettings
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

class AIAgentService {
    
    private val client = OkHttpClient()
    private val gson = Gson()
    private val JSON = "application/json; charset=utf-8".toMediaType()
    
    data class AIRequest(
        val prompt: String,
        val context: Map<String, Any> = emptyMap(),
        val conversationHistory: List<Message> = emptyList()
    )
    
    data class Message(
        val role: String, // "user" or "assistant"
        val content: String
    )
    
    data class AIResponse(
        val content: String,
        val question: String? = null, // If AI needs clarification
        val requiresInput: Boolean = false,
        val completed: Boolean = true
    )
    
    fun generateCode(
        request: AIRequest,
        onResponse: (AIResponse) -> Unit,
        onError: (String) -> Unit
    ) {
        val settings = AIAssistantSettings.getInstance().state
        
        if (settings.apiKey.isEmpty()) {
            onError("API key not configured. Please set it in Settings > AI Code Assistant")
            return
        }
        
        val requestBody = JsonObject().apply {
            addProperty("prompt", request.prompt)
            add("context", gson.toJsonTree(request.context))
            add("conversation_history", gson.toJsonTree(request.conversationHistory))
            addProperty("max_tokens", settings.maxTokens)
            addProperty("temperature", settings.temperature)
        }
        
        val httpRequest = Request.Builder()
            .url(settings.apiEndpoint)
            .addHeader("Authorization", "Bearer ${settings.apiKey}")
            .addHeader("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody(JSON))
            .build()
        
        client.newCall(httpRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onError("Network error: ${e.message}")
            }
            
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        onError("API error: ${response.code} - ${response.message}")
                        return
                    }
                    
                    val responseBody = response.body?.string()
                    if (responseBody == null) {
                        onError("Empty response from API")
                        return
                    }
                    
                    try {
                        val aiResponse = gson.fromJson(responseBody, AIResponse::class.java)
                        onResponse(aiResponse)
                    } catch (e: Exception) {
                        onError("Failed to parse response: ${e.message}")
                    }
                }
            }
        })
    }
    
    companion object {
        fun getInstance(): AIAgentService = AIAgentService()
    }
}
```

### 5. Interactive Tool Window

```kotlin
// AIAssistantToolWindowFactory.kt
package com.yourcompany.aiassistant.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

class AIAssistantToolWindowFactory : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val chatPanel = AIAssistantChatPanel(project)
        val content = ContentFactory.getInstance().createContent(chatPanel, "", false)
        toolWindow.contentManager.addContent(content)
    }
}
```

```kotlin
// AIAssistantChatPanel.kt
package com.yourcompany.aiassistant.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.yourcompany.aiassistant.service.AIAgentService
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class AIAssistantChatPanel(private val project: Project) : JPanel(BorderLayout()) {
    
    private val chatArea = JBTextArea()
    private val inputField = JBTextArea(3, 50)
    private val sendButton = JButton("Send")
    private val aiService = AIAgentService.getInstance()
    
    private val conversationHistory = mutableListOf<AIAgentService.Message>()
    
    init {
        setupUI()
        setupListeners()
    }
    
    private fun setupUI() {
        chatArea.isEditable = false
        chatArea.lineWrap = true
        chatArea.wrapStyleWord = true
        
        inputField.lineWrap = true
        inputField.wrapStyleWord = true
        
        val scrollPane = JBScrollPane(chatArea)
        scrollPane.preferredSize = Dimension(400, 500)
        
        val inputPanel = JPanel(BorderLayout()).apply {
            add(JBScrollPane(inputField), BorderLayout.CENTER)
            add(sendButton, BorderLayout.EAST)
        }
        
        add(scrollPane, BorderLayout.CENTER)
        add(inputPanel, BorderLayout.SOUTH)
        
        appendToChat("AI Assistant", "Hello! I can help you with:\n" +
                "- Generating code snippets\n" +
                "- Completing existing code\n" +
                "- Creating entire project structures\n\n" +
                "What would you like me to help with?")
    }
    
    private fun setupListeners() {
        sendButton.addActionListener { sendMessage() }
        
        inputField.inputMap.put(
            KeyStroke.getKeyStroke("control ENTER"),
            "send"
        )
        inputField.actionMap.put("send", object : AbstractAction() {
            override fun actionPerformed(e: java.awt.event.ActionEvent?) {
                sendMessage()
            }
        })
    }
    
    private fun sendMessage() {
        val message = inputField.text.trim()
        if (message.isEmpty()) return
        
        appendToChat("You", message)
        inputField.text = ""
        
        conversationHistory.add(AIAgentService.Message("user", message))
        
        sendButton.isEnabled = false
        appendToChat("AI", "Thinking...")
        
        val context = buildContext()
        
        aiService.generateCode(
            AIAgentService.AIRequest(
                prompt = message,
                context = context,
                conversationHistory = conversationHistory
            ),
            onResponse = { response ->
                ApplicationManager.getApplication().invokeLater {
                    removeLastMessage() // Remove "Thinking..."
                    
                    conversationHistory.add(
                        AIAgentService.Message("assistant", response.content)
                    )
                    
                    appendToChat("AI", response.content)
                    
                    if (response.requiresInput && response.question != null) {
                        appendToChat("AI", "\n${response.question}")
                    }
                    
                    sendButton.isEnabled = true
                }
            },
            onError = { error ->
                ApplicationManager.getApplication().invokeLater {
                    removeLastMessage()
                    appendToChat("Error", error)
                    sendButton.isEnabled = true
                }
            }
        )
    }
    
    private fun buildContext(): Map<String, Any> {
        return mapOf(
            "project_name" to (project.name ?: ""),
            "project_path" to (project.basePath ?: "")
        )
    }
    
    private fun appendToChat(sender: String, message: String) {
        chatArea.append("\n[$sender]\n$message\n")
        chatArea.caretPosition = chatArea.document.length
    }
    
    private fun removeLastMessage() {
        val text = chatArea.text
        val lastIndex = text.lastIndexOf("\n[AI]\n")
        if (lastIndex != -1) {
            chatArea.text = text.substring(0, lastIndex)
        }
    }
}
```

### 6. Action Classes

```kotlin
// GenerateCodeAction.kt
package com.yourcompany.aiassistant.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.ui.Messages
import com.yourcompany.aiassistant.service.AIAgentService

class GenerateCodeAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return
        
        val requirement = Messages.showInputDialog(
            project,
            "Describe what code you want to generate:",
            "Generate Code with AI",
            Messages.getQuestionIcon()
        ) ?: return
        
        val aiService = AIAgentService.getInstance()
        
        aiService.generateCode(
            AIAgentService.AIRequest(
                prompt = "Generate code for: $requirement",
                context = mapOf(
                    "file_type" to (editor.virtualFile?.extension ?: ""),
                    "current_position" to editor.caretModel.offset
                )
            ),
            onResponse = { response ->
                ApplicationManager.getApplication().invokeLater {
                    if (response.requiresInput && response.question != null) {
                        val answer = Messages.showInputDialog(
                            project,
                            response.question,
                            "AI Needs More Information",
                            Messages.getQuestionIcon()
                        )
                        
                        if (answer != null) {
                            // Recursive call with more context
                            handleFollowUpQuestion(project, editor, requirement, answer)
                        }
                    } else {
                        insertCode(project, editor, response.content)
                    }
                }
            },
            onError = { error ->
                ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog(project, error, "AI Error")
                }
            }
        )
    }
    
    private fun handleFollowUpQuestion(
        project: com.intellij.openapi.project.Project,
        editor: com.intellij.openapi.editor.Editor,
        originalRequirement: String,
        answer: String
    ) {
        val aiService = AIAgentService.getInstance()
        aiService.generateCode(
            AIAgentService.AIRequest(
                prompt = originalRequirement,
                conversationHistory = listOf(
                    AIAgentService.Message("user", originalRequirement),
                    AIAgentService.Message("user", answer)
                )
            ),
            onResponse = { response ->
                ApplicationManager.getApplication().invokeLater {
                    insertCode(project, editor, response.content)
                }
            },
            onError = { error ->
                ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog(project, error, "AI Error")
                }
            }
        )
    }
    
    private fun insertCode(
        project: com.intellij.openapi.project.Project,
        editor: com.intellij.openapi.editor.Editor,
        code: String
    ) {
        WriteCommandAction.runWriteCommandAction(project) {
            val document = editor.document
            val offset = editor.caretModel.offset
            document.insertString(offset, code)
        }
    }
}
```

```kotlin
// GenerateProjectAction.kt
package com.yourcompany.aiassistant.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile
import com.yourcompany.aiassistant.service.AIAgentService
import com.yourcompany.aiassistant.ui.ProjectGenerationDialog
import java.io.File

class GenerateProjectAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        
        val dialog = ProjectGenerationDialog(project)
        if (!dialog.showAndGet()) return
        
        val requirements = dialog.getRequirements()
        val targetDir = dialog.getTargetDirectory()
        
        val aiService = AIAgentService.getInstance()
        
        generateProjectStructure(
            aiService,
            requirements,
            targetDir,
            project
        )
    }
    
    private fun generateProjectStructure(
        aiService: AIAgentService,
        requirements: String,
        targetDir: VirtualFile,
        project: com.intellij.openapi.project.Project
    ) {
        aiService.generateCode(
            AIAgentService.AIRequest(
                prompt = "Generate a complete project structure for: $requirements",
                context = mapOf(
                    "target_directory" to targetDir.path,
                    "generation_type" to "full_project"
                )
            ),
            onResponse = { response ->
                ApplicationManager.getApplication().invokeLater {
                    // Parse and create project files
                    createProjectFiles(targetDir, response.content)
                    Messages.showInfoMessage(
                        project,
                        "Project generated successfully!",
                        "Success"
                    )
                }
            },
            onError = { error ->
                ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog(project, error, "Generation Failed")
                }
            }
        )
    }
    
    private fun createProjectFiles(baseDir: VirtualFile, projectStructure: String) {
        // Parse the AI response and create files
        // This would depend on your AI's response format
        ApplicationManager.getApplication().runWriteAction {
            // Example: Create files based on AI response
            // You'll need to parse the response format from your AI
        }
    }
}
```

## Key Features

This plugin provides:

1. **Settings page** to configure API key and endpoint
2. **Interactive chat panel** for conversational code generation with question/answer capability
3. **Context-aware actions** that understand current file and cursor position
4. **Follow-up questions** - AI can ask for clarification
5. **Full project generation** from requirements

## Next Steps

1. Adapt the API request/response format to match your AI agent's actual API
2. Add proper error handling and retry logic
3. Implement file creation logic based on your AI's response format
4. Add syntax highlighting in the chat panel
5. Implement code preview before insertion
6. Add streaming responses if your API supports it

Would you like me to elaborate on any specific part or help you customize it for your particular AI agent API?


# Secondly

I'll enhance the plugin to work with your REST API gateway setup. This will include better error handling, retry logic, streaming support, and a more robust architecture.

## Enhanced Project Structure

### 1. Updated `build.gradle.kts`

```kotlin
plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.9.0"
    id("org.jetbrains.intellij") version "1.16.0"
}

group = "com.yourcompany"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.7.3")
}

intellij {
    version.set("2023.2")
    type.set("IC")
    plugins.set(listOf())
}

tasks {
    patchPluginXml {
        sinceBuild.set("232")
        untilBuild.set("242.*")
    }
    
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }
}
```

### 2. Enhanced Settings with Gateway Configuration

```kotlin
// AIAssistantSettings.kt
package com.yourcompany.aiassistant.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage

@State(
    name = "AIAssistantSettings",
    storages = [Storage("AIAssistantSettings.xml")]
)
class AIAssistantSettings : PersistentStateComponent<AIAssistantSettings.State> {
    
    data class State(
        // API Gateway Configuration
        var gatewayUrl: String = "https://your-gateway.com/api/ai",
        var apiKey: String = "",
        var apiSecret: String = "",
        
        // Request Configuration
        var maxTokens: Int = 4000,
        var temperature: Double = 0.7,
        var timeout: Int = 60, // seconds
        var maxRetries: Int = 3,
        
        // Feature Flags
        var enableStreaming: Boolean = false,
        var enableConversationHistory: Boolean = true,
        var maxHistorySize: Int = 10,
        
        // Advanced Settings
        var customHeaders: Map<String, String> = emptyMap(),
        var enableLogging: Boolean = false
    )
    
    private var state = State()
    
    override fun getState(): State = state
    
    override fun loadState(state: State) {
        this.state = state
    }
    
    companion object {
        fun getInstance(): AIAssistantSettings {
            return ApplicationManager.getApplication()
                .getService(AIAssistantSettings::class.java)
        }
    }
}
```

### 3. Enhanced Settings UI

```kotlin
// AIAssistantConfigurable.kt
package com.yourcompany.aiassistant.settings

import com.intellij.openapi.options.Configurable
import com.intellij.ui.components.*
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.*

class AIAssistantConfigurable : Configurable {
    private var gatewayUrlField: JBTextField? = null
    private var apiKeyField: JBPasswordField? = null
    private var apiSecretField: JBPasswordField? = null
    private var maxTokensField: JBTextField? = null
    private var temperatureField: JBTextField? = null
    private var timeoutField: JBTextField? = null
    private var maxRetriesField: JBTextField? = null
    private var enableStreamingCheckbox: JBCheckBox? = null
    private var enableHistoryCheckbox: JBCheckBox? = null
    private var maxHistorySizeField: JBTextField? = null
    private var enableLoggingCheckbox: JBCheckBox? = null
    private var customHeadersArea: JBTextArea? = null
    private var testConnectionButton: JButton? = null
    
    override fun getDisplayName(): String = "AI Code Assistant"
    
    override fun createComponent(): JComponent {
        gatewayUrlField = JBTextField()
        apiKeyField = JBPasswordField()
        apiSecretField = JBPasswordField()
        maxTokensField = JBTextField()
        temperatureField = JBTextField()
        timeoutField = JBTextField()
        maxRetriesField = JBTextField()
        enableStreamingCheckbox = JBCheckBox("Enable Streaming Responses")
        enableHistoryCheckbox = JBCheckBox("Enable Conversation History")
        maxHistorySizeField = JBTextField()
        enableLoggingCheckbox = JBCheckBox("Enable Debug Logging")
        customHeadersArea = JBTextArea(5, 50)
        testConnectionButton = JButton("Test Connection")
        
        testConnectionButton?.addActionListener {
            testConnection()
        }
        
        val mainPanel = JPanel(BorderLayout())
        
        val formPanel = FormBuilder.createFormBuilder()
            .addComponent(JBLabel("<html><b>API Gateway Configuration</b></html>"))
            .addLabeledComponent(JBLabel("Gateway URL:"), gatewayUrlField!!, 1, false)
            .addLabeledComponent(JBLabel("API Key:"), apiKeyField!!, 1, false)
            .addLabeledComponent(JBLabel("API Secret:"), apiSecretField!!, 1, false)
            .addComponent(testConnectionButton!!)
            .addVerticalGap(10)
            
            .addComponent(JBLabel("<html><b>Request Configuration</b></html>"))
            .addLabeledComponent(JBLabel("Max Tokens:"), maxTokensField!!, 1, false)
            .addLabeledComponent(JBLabel("Temperature (0.0-1.0):"), temperatureField!!, 1, false)
            .addLabeledComponent(JBLabel("Timeout (seconds):"), timeoutField!!, 1, false)
            .addLabeledComponent(JBLabel("Max Retries:"), maxRetriesField!!, 1, false)
            .addVerticalGap(10)
            
            .addComponent(JBLabel("<html><b>Features</b></html>"))
            .addComponent(enableStreamingCheckbox!!)
            .addComponent(enableHistoryCheckbox!!)
            .addLabeledComponent(JBLabel("Max History Size:"), maxHistorySizeField!!, 1, false)
            .addComponent(enableLoggingCheckbox!!)
            .addVerticalGap(10)
            
            .addComponent(JBLabel("<html><b>Custom Headers</b> (JSON format, e.g., {\"X-Custom\": \"value\"})</html>"))
            .addComponentFillVertically(JBScrollPane(customHeadersArea!!), 0)
            .panel
        
        mainPanel.add(formPanel, BorderLayout.NORTH)
        mainPanel.border = JBUI.Borders.empty(10)
        
        return JBScrollPane(mainPanel)
    }
    
    private fun testConnection() {
        testConnectionButton?.isEnabled = false
        testConnectionButton?.text = "Testing..."
        
        // Test connection in background
        Thread {
            try {
                val service = com.yourcompany.aiassistant.service.APIGatewayService.getInstance()
                val result = service.testConnection(
                    gatewayUrlField?.text ?: "",
                    apiKeyField?.password?.concatToString() ?: "",
                    apiSecretField?.password?.concatToString() ?: ""
                )
                
                SwingUtilities.invokeLater {
                    if (result) {
                        JOptionPane.showMessageDialog(
                            testConnectionButton,
                            "Connection successful!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE
                        )
                    } else {
                        JOptionPane.showMessageDialog(
                            testConnectionButton,
                            "Connection failed. Please check your credentials.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE
                        )
                    }
                    testConnectionButton?.isEnabled = true
                    testConnectionButton?.text = "Test Connection"
                }
            } catch (e: Exception) {
                SwingUtilities.invokeLater {
                    JOptionPane.showMessageDialog(
                        testConnectionButton,
                        "Connection error: ${e.message}",
                        "Error",
                        JOptionPane.ERROR_MESSAGE
                    )
                    testConnectionButton?.isEnabled = true
                    testConnectionButton?.text = "Test Connection"
                }
            }
        }.start()
    }
    
    override fun isModified(): Boolean {
        val settings = AIAssistantSettings.getInstance().state
        return gatewayUrlField?.text != settings.gatewayUrl ||
               apiKeyField?.password?.concatToString() != settings.apiKey ||
               apiSecretField?.password?.concatToString() != settings.apiSecret ||
               maxTokensField?.text != settings.maxTokens.toString() ||
               temperatureField?.text != settings.temperature.toString() ||
               timeoutField?.text != settings.timeout.toString() ||
               maxRetriesField?.text != settings.maxRetries.toString() ||
               enableStreamingCheckbox?.isSelected != settings.enableStreaming ||
               enableHistoryCheckbox?.isSelected != settings.enableConversationHistory ||
               maxHistorySizeField?.text != settings.maxHistorySize.toString() ||
               enableLoggingCheckbox?.isSelected != settings.enableLogging ||
               customHeadersArea?.text != formatCustomHeaders(settings.customHeaders)
    }
    
    override fun apply() {
        val settings = AIAssistantSettings.getInstance()
        settings.state.gatewayUrl = gatewayUrlField?.text ?: ""
        settings.state.apiKey = apiKeyField?.password?.concatToString() ?: ""
        settings.state.apiSecret = apiSecretField?.password?.concatToString() ?: ""
        settings.state.maxTokens = maxTokensField?.text?.toIntOrNull() ?: 4000
        settings.state.temperature = temperatureField?.text?.toDoubleOrNull() ?: 0.7
        settings.state.timeout = timeoutField?.text?.toIntOrNull() ?: 60
        settings.state.maxRetries = maxRetriesField?.text?.toIntOrNull() ?: 3
        settings.state.enableStreaming = enableStreamingCheckbox?.isSelected ?: false
        settings.state.enableConversationHistory = enableHistoryCheckbox?.isSelected ?: true
        settings.state.maxHistorySize = maxHistorySizeField?.text?.toIntOrNull() ?: 10
        settings.state.enableLogging = enableLoggingCheckbox?.isSelected ?: false
        settings.state.customHeaders = parseCustomHeaders(customHeadersArea?.text ?: "")
    }
    
    override fun reset() {
        val settings = AIAssistantSettings.getInstance().state
        gatewayUrlField?.text = settings.gatewayUrl
        apiKeyField?.text = settings.apiKey
        apiSecretField?.text = settings.apiSecret
        maxTokensField?.text = settings.maxTokens.toString()
        temperatureField?.text = settings.temperature.toString()
        timeoutField?.text = settings.timeout.toString()
        maxRetriesField?.text = settings.maxRetries.toString()
        enableStreamingCheckbox?.isSelected = settings.enableStreaming
        enableHistoryCheckbox?.isSelected = settings.enableConversationHistory
        maxHistorySizeField?.text = settings.maxHistorySize.toString()
        enableLoggingCheckbox?.isSelected = settings.enableLogging
        customHeadersArea?.text = formatCustomHeaders(settings.customHeaders)
    }
    
    private fun parseCustomHeaders(text: String): Map<String, String> {
        if (text.trim().isEmpty()) return emptyMap()
        return try {
            com.google.gson.Gson().fromJson(text, Map::class.java)
                .mapKeys { it.key.toString() }
                .mapValues { it.value.toString() }
        } catch (e: Exception) {
            emptyMap()
        }
    }
    
    private fun formatCustomHeaders(headers: Map<String, String>): String {
        if (headers.isEmpty()) return ""
        return com.google.gson.GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(headers)
    }
}
```

### 4. Enhanced API Gateway Service

```kotlin
// APIGatewayService.kt
package com.yourcompany.aiassistant.service

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.yourcompany.aiassistant.settings.AIAssistantSettings
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import java.io.IOException
import java.util.concurrent.TimeUnit

class APIGatewayService {
    
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()
    
    private val JSON = "application/json; charset=utf-8".toMediaType()
    
    private var client: OkHttpClient? = null
    
    data class GatewayRequest(
        val prompt: String,
        val context: RequestContext? = null,
        val parameters: RequestParameters? = null
    )
    
    data class RequestContext(
        val conversationHistory: List<Message>? = null,
        val fileContext: FileContext? = null,
        val projectContext: ProjectContext? = null
    )
    
    data class FileContext(
        val fileName: String? = null,
        val fileType: String? = null,
        val currentContent: String? = null,
        val cursorPosition: Int? = null,
        val selectedText: String? = null
    )
    
    data class ProjectContext(
        val projectName: String? = null,
        val projectPath: String? = null,
        val projectType: String? = null,
        val dependencies: List<String>? = null
    )
    
    data class RequestParameters(
        val maxTokens: Int? = null,
        val temperature: Double? = null,
        val stream: Boolean? = false
    )
    
    data class Message(
        val role: String, // "user" or "assistant"
        val content: String,
        val timestamp: Long = System.currentTimeMillis()
    )
    
    data class GatewayResponse(
        val success: Boolean,
        val message: String? = null,
        val content: String? = null,
        val question: String? = null,
        val requiresInput: Boolean = false,
        val metadata: ResponseMetadata? = null,
        val error: ErrorDetails? = null
    )
    
    data class ResponseMetadata(
        val tokensUsed: Int? = null,
        val processingTime: Long? = null,
        val modelVersion: String? = null
    )
    
    data class ErrorDetails(
        val code: String? = null,
        val message: String? = null,
        val details: String? = null
    )
    
    interface StreamCallback {
        fun onChunk(chunk: String)
        fun onComplete()
        fun onError(error: String)
    }
    
    private fun getClient(): OkHttpClient {
        if (client == null) {
            val settings = AIAssistantSettings.getInstance().state
            
            val builder = OkHttpClient.Builder()
                .connectTimeout(settings.timeout.toLong(), TimeUnit.SECONDS)
                .readTimeout(settings.timeout.toLong(), TimeUnit.SECONDS)
                .writeTimeout(settings.timeout.toLong(), TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
            
            // Add logging interceptor if enabled
            if (settings.enableLogging) {
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                builder.addInterceptor(loggingInterceptor)
            }
            
            // Add authentication interceptor
            builder.addInterceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("X-API-Key", settings.apiKey)
                
                if (settings.apiSecret.isNotEmpty()) {
                    requestBuilder.header("X-API-Secret", settings.apiSecret)
                }
                
                // Add custom headers
                settings.customHeaders.forEach { (key, value) ->
                    requestBuilder.header(key, value)
                }
                
                chain.proceed(requestBuilder.build())
            }
            
            client = builder.build()
        }
        return client!!
    }
    
    fun sendRequest(
        request: GatewayRequest,
        onSuccess: (GatewayResponse) -> Unit,
        onError: (String) -> Unit,
        streamCallback: StreamCallback? = null
    ) {
        val settings = AIAssistantSettings.getInstance().state
        
        if (settings.gatewayUrl.isEmpty()) {
            onError("Gateway URL not configured. Please configure in Settings > AI Code Assistant")
            return
        }
        
        if (settings.apiKey.isEmpty()) {
            onError("API Key not configured. Please configure in Settings > AI Code Assistant")
            return
        }
        
        val requestBody = JsonObject().apply {
            addProperty("prompt", request.prompt)
            
            if (request.context != null) {
                add("context", gson.toJsonTree(request.context))
            }
            
            val params = request.parameters ?: RequestParameters(
                maxTokens = settings.maxTokens,
                temperature = settings.temperature,
                stream = settings.enableStreaming && streamCallback != null
            )
            add("parameters", gson.toJsonTree(params))
        }
        
        val httpRequest = Request.Builder()
            .url(settings.gatewayUrl)
            .post(requestBody.toString().toRequestBody(JSON))
            .build()
        
        if (settings.enableStreaming && streamCallback != null) {
            handleStreamingRequest(httpRequest, streamCallback, onError)
        } else {
            handleRegularRequest(httpRequest, onSuccess, onError, settings.maxRetries)
        }
    }
    
    private fun handleRegularRequest(
        request: Request,
        onSuccess: (GatewayResponse) -> Unit,
        onError: (String) -> Unit,
        retriesLeft: Int = 3
    ) {
        getClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                if (retriesLeft > 0) {
                    // Retry with exponential backoff
                    Thread.sleep((4 - retriesLeft) * 1000L)
                    handleRegularRequest(request, onSuccess, onError, retriesLeft - 1)
                } else {
                    onError("Network error after retries: ${e.message}")
                }
            }
            
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    try {
                        if (!response.isSuccessful) {
                            val errorBody = response.body?.string()
                            val errorMsg = try {
                                val errorResponse = gson.fromJson(errorBody, GatewayResponse::class.java)
                                errorResponse.error?.message ?: "HTTP ${response.code}: ${response.message}"
                            } catch (e: Exception) {
                                "HTTP ${response.code}: ${response.message}"
                            }
                            
                            if (response.code in 500..599 && retriesLeft > 0) {
                                // Retry on server errors
                                Thread.sleep((4 - retriesLeft) * 1000L)
                                handleRegularRequest(request, onSuccess, onError, retriesLeft - 1)
                            } else {
                                onError(errorMsg)
                            }
                            return
                        }
                        
                        val responseBody = response.body?.string()
                        if (responseBody == null) {
                            onError("Empty response from gateway")
                            return
                        }
                        
                        val gatewayResponse = gson.fromJson(responseBody, GatewayResponse::class.java)
                        
                        if (gatewayResponse.success) {
                            onSuccess(gatewayResponse)
                        } else {
                            onError(gatewayResponse.error?.message ?: "Request failed")
                        }
                        
                    } catch (e: Exception) {
                        onError("Failed to parse response: ${e.message}")
                    }
                }
            }
        })
    }
    
    private fun handleStreamingRequest(
        request: Request,
        streamCallback: StreamCallback,
        onError: (String) -> Unit
    ) {
        getClient().newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                streamCallback.onError("Network error: ${e.message}")
            }
            
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        streamCallback.onError("HTTP ${response.code}: ${response.message}")
                        return
                    }
                    
                    try {
                        response.body?.charStream()?.use { reader ->
                            reader.forEachLine { line ->
                                if (line.startsWith("data: ")) {
                                    val data = line.substring(6)
                                    if (data == "[DONE]") {
                                        streamCallback.onComplete()
                                    } else {
                                        try {
                                            val chunk = gson.fromJson(data, JsonObject::class.java)
                                            val content = chunk.get("content")?.asString
                                            if (content != null) {
                                                streamCallback.onChunk(content)
                                            }
                                        } catch (e: Exception) {
                                            // Ignore malformed chunks
                                        }
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        streamCallback.onError("Streaming error: ${e.message}")
                    }
                }
            }
        })
    }
    
    fun testConnection(url: String, apiKey: String, apiSecret: String): Boolean {
        return try {
            val testClient = OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(10, TimeUnit.SECONDS)
                .build()
            
            val testRequest = Request.Builder()
                .url(url)
                .header("X-API-Key", apiKey)
                .apply {
                    if (apiSecret.isNotEmpty()) {
                        header("X-API-Secret", apiSecret)
                    }
                }
                .post(JsonObject().apply {
                    addProperty("prompt", "test")
                }.toString().toRequestBody(JSON))
                .build()
            
            val response = testClient.newCall(testRequest).execute()
            response.close()
            response.isSuccessful
        } catch (e: Exception) {
            false
        }
    }
    
    companion object {
        fun getInstance(): APIGatewayService = APIGatewayService()
    }
}
```

### 5. Enhanced Chat Panel with Streaming Support

```kotlin
// AIAssistantChatPanel.kt
package com.yourcompany.aiassistant.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.yourcompany.aiassistant.service.APIGatewayService
import com.yourcompany.aiassistant.settings.AIAssistantSettings
import java.awt.*
import java.text.SimpleDateFormat
import java.util.*
import javax.swing.*

class AIAssistantChatPanel(private val project: Project) : JPanel(BorderLayout()) {
    
    private val chatContainer = JPanel()
    private val inputField = JBTextArea(3, 50)
    private val sendButton = JButton("Send")
    private val clearButton = JButton("Clear")
    private val stopButton = JButton("Stop")
    private val gatewayService = APIGatewayService.getInstance()
    
    private val conversationHistory = mutableListOf<APIGatewayService.Message>()
    private val dateFormat = SimpleDateFormat("HH:mm:ss")
    
    private var isGenerating = false
    private var currentStreamingMessage: MessageBubble? = null
    
    init {
        setupUI()
        setupListeners()
    }
    
    private fun setupUI() {
        // Chat container setup
        chatContainer.layout = BoxLayout(chatContainer, BoxLayout.Y_AXIS)
        chatContainer.border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        
        val chatScrollPane = JBScrollPane(chatContainer)
        chatScrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_ALWAYS
        chatScrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
        
        // Input field setup
        inputField.lineWrap = true
        inputField.wrapStyleWord = true
        inputField.border = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(JBColor.border()),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        )
        
        // Button panel
        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT))
        stopButton.isEnabled = false
        buttonPanel.add(clearButton)
        buttonPanel.add(stopButton)
        buttonPanel.add(sendButton)
        
        // Input panel
        val inputPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(5, 10, 10, 10)
            add(JBScrollPane(inputField), BorderLayout.CENTER)
            add(buttonPanel, BorderLayout.SOUTH)
        }
        
        add(chatScrollPane, BorderLayout.CENTER)
        add(inputPanel, BorderLayout.SOUTH)
        
        // Welcome message
        addAssistantMessage("Hello! I'm your AI Code Assistant. I can help you with:\n\n" +
                "• Generating code snippets\n" +
                "• Completing existing code\n" +
                "• Creating entire project structures\n" +
                "• Answering programming questions\n" +
                "• Refactoring and improving code\n\n" +
                "What would you like me to help with?")
    }
    
    private fun setupListeners() {
        sendButton.addActionListener { sendMessage() }
        clearButton.addActionListener { clearConversation() }
        stopButton.addActionListener { stopGeneration() }
        
        // Ctrl+Enter to send
        inputField.inputMap.put(KeyStroke.getKeyStroke("control ENTER"), "send")
        inputField.actionMap.put("send", object : AbstractAction() {
            override fun actionPerformed(e: java.awt.event.ActionEvent?) {
                sendMessage()
            }
        })
        
        // Shift+Enter for newline (default behavior)
    }
    
    private fun sendMessage() {
        val message = inputField.text.trim()
        if (message.isEmpty() || isGenerating) return
        
        addUserMessage(message)
        inputField.text = ""
        
        val userMessage = APIGatewayService.Message("user", message)
        conversationHistory.add(userMessage)
        
        // Trim history if needed
        val settings = AIAssistantSettings.getInstance().state
        if (conversationHistory.size > settings.maxHistorySize * 2) {
            conversationHistory.subList(0, conversationHistory.size - settings.maxHistorySize * 2).clear()
        }
        
        isGenerating = true
        sendButton.isEnabled = false
        stopButton.isEnabled = true
        
        val context = buildContext()
        
        val request = APIGatewayService.GatewayRequest(
            prompt = message,
            context = context
        )
        
        if (settings.enableStreaming) {
            handleStreamingResponse(request)
        } else {
            handleRegularResponse(request)
        }
    }
    
    private fun handleStreamingResponse(request: APIGatewayService.GatewayRequest) {
        val messageBubble = addAssistantMessage("", isStreaming = true)
        currentStreamingMessage = messageBubble
        
        gatewayService.sendRequest(
            request = request,
            onSuccess = { /* Not used in streaming */ },
            onError = { error ->
                ApplicationManager.getApplication().invokeLater {
                    messageBubble.setError(error)
                    finishGeneration()
                }
            },
            streamCallback = object : APIGatewayService.StreamCallback {
                override fun onChunk(chunk: String) {
                    ApplicationManager.getApplication().invokeLater {
                        messageBubble.appendContent(chunk)
                        scrollToBottom()
                    }
                }
                
                override fun onComplete() {
                    ApplicationManager.getApplication().invokeLater {
                        messageBubble.setComplete()
                        conversationHistory.add(
                            APIGatewayService.Message("assistant", messageBubble.getContent())
                        )
                        finishGeneration()
                    }
                }
                
                override fun onError(error: String) {
                    ApplicationManager.getApplication().invokeLater {
                        messageBubble.setError(error)
                        finishGeneration()
                    }
                }
            }
        )
    }
    
    private fun handleRegularResponse(request: APIGatewayService.GatewayRequest) {
        val thinkingBubble = addAssistantMessage("Thinking...", isThinking = true)
        
        gatewayService.sendRequest(
            request = request,
            onSuccess = { response ->
                ApplicationManager.getApplication().invokeLater {
                    chatContainer.remove(thinkingBubble)
                    
                    if (response.content != null) {
                        addAssistantMessage(response.content)
                        conversationHistory.add(
                            APIGatewayService.Message("assistant", response.content)
                        )
                        
                        if (response.requiresInput && response.question != null) {
                            addAssistantMessage("⚠️ ${response.question}", isQuestion = true)
                        }
                    } else if (response.error != null) {
                        addErrorMessage(response.error.message ?: "Unknown error")
                    }
                    
                    finishGeneration()
                }
            },
            onError = { error ->
                ApplicationManager.getApplication().invokeLater {
                    chatContainer.remove(thinkingBubble)
                    addErrorMessage(error)
                    finishGeneration()
                }
            }
        )
    }
    
    private fun buildContext(): APIGatewayService.RequestContext {
        val settings = AIAssistantSettings.getInstance().state
        
        val history = if (settings.enableConversationHistory) {
            conversationHistory.takeLast(settings.maxHistorySize * 2)
        } else {
            null
        }
        
        return APIGatewayService.RequestContext(
            conversationHistory = history,
            projectContext = APIGatewayService.ProjectContext(
                projectName = project.name,
                projectPath = project.basePath
            )
        )
    }
    
    private fun addUserMessage(content: String) {
        val bubble = MessageBubble(content, MessageType.USER)
        chatContainer.add(bubble)
        chatContainer.add(Box.createVerticalStrut(10))
        scrollToBottom()
    }
    
    private fun addAssistantMessage(
        content: String,
        isStreaming: Boolean = false,
        isThinking: Boolean = false,
        isQuestion: Boolean = false
    ): MessageBubble {
        val type = when {
            isStreaming -> MessageType.ASSISTANT_STREAMING
            isThinking -> MessageType.THINKING
            isQuestion -> MessageType.QUESTION
            else -> MessageType.ASSISTANT
        }
        
        val bubble = MessageBubble(content, type)
        chatContainer.add(bubble)
        chatContainer.add(Box.createVerticalStrut(10))
        scrollToBottom()
        return bubble
    }
    
    private fun addErrorMessage(error: String) {
        val bubble = MessageBubble(error, MessageType.ERROR)
        chatContainer.add(bubble)
        chatContainer.add(Box.createVerticalStrut(10))
        scrollToBottom()
    }
    
    private fun clearConversation() {
        val result = JOptionPane.showConfirmDialog(
            this,
            "Clear all conversation history?",
            "Clear Conversation",
            JOptionPane.YES_NO_OPTION
        )
        
        if (result == JOptionPane.YES_OPTION) {
            conversationHistory.clear()
            chatContainer.removeAll()
            chatContainer.revalidate()
            chatContainer.repaint()
            addAssistantMessage("Conversation cleared. How can I help you?")
        }
    }
    
    private fun stopGeneration() {
        isGenerating = false
        currentStreamingMessage?.setComplete()
        finishGeneration()
    }
    
    private fun finishGeneration() {
        isGenerating = false
        sendButton.isEnabled = true
        stopButton.isEnabled = false
        currentStreamingMessage = null
        scrollToBottom()
    }
    
    private fun scrollToBottom() {
        SwingUtilities.invokeLater {
            val scrollPane = SwingUtilities.getAncestorOfClass(JScrollPane::class.java, chatContainer) as? JScrollPane
            scrollPane?.verticalScrollBar?.value = scrollPane?.verticalScrollBar?.maximum ?: 0
        }
    }
    
    enum class MessageType {
        USER, ASSISTANT, ASSISTANT_STREAMING, THINKING, QUESTION, ERROR
    }
    
    inner class MessageBubble(
        initialContent: String,
        private val type: MessageType
    ) : JPanel(BorderLayout()) {
        
        private val contentArea = JBTextArea()
        private val timestampLabel = JBLabel(dateFormat.format(Date()))
        private val iconLabel = JBLabel()
        private var content = initialContent
        
        init {
            setupUI()
            updateContent(initialContent)
        }
        
        private fun setupUI() {
            val bgColor = when (type) {
                MessageType.USER -> JBColor(Color(230, 240, 255), Color(40, 50, 60))
                MessageType.ASSISTANT, MessageType.ASSISTANT_STREAMING -> JBColor(Color(240, 255, 240), Color(50, 60, 50))
                MessageType.THINKING -> JBColor(Color(255, 255, 230), Color(60, 60, 40))
                MessageType.QUESTION -> JBColor(Color(255, 245, 230), Color(60, 55, 40))
                MessageType.ERROR -> JBColor(Color(255, 230, 230), Color(60, 40, 40))
            }
            
            val icon = when (type) {
                MessageType.USER -> "👤"
                MessageType.ASSISTANT, MessageType.ASSISTANT_STREAMING -> "🤖"
                MessageType.THINKING -> "💭"
                MessageType.QUESTION -> "❓"
                MessageType.ERROR -> "❌"
            }
            
            iconLabel.text = icon
            iconLabel.font = iconLabel.font.deriveFont(20f)
            
            contentArea.isEditable = false
            contentArea.lineWrap = true
            contentArea.wrapStyleWord = true
            contentArea.background = bgColor
            contentArea.border = null
            
            timestampLabel.foreground = JBColor.GRAY
            timestampLabel.font = timestampLabel.font.deriveFont(10f)
            
            val headerPanel = JPanel(BorderLayout()).apply {
                background = bgColor
                add(iconLabel, BorderLayout.WEST)
                add(timestampLabel, BorderLayout.EAST)
                border = BorderFactory.createEmptyBorder(5, 5, 0, 5)
            }
            
            background = bgColor
            border = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(JBColor.border(), 1, true),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
            )
            
            maximumSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)
            
            add(headerPanel, BorderLayout.NORTH)
            add(contentArea, BorderLayout.CENTER)
        }
        
        fun appendContent(chunk: String) {
            content += chunk
            updateContent(content)
        }
        
        fun setComplete() {
            // Mark as complete, could add an indicator
        }
        
        fun setError(error: String) {
            content = "❌ Error: $error"
            updateContent(content)
        }
        
        fun getContent(): String = content
        
        private fun updateContent(text: String) {
            contentArea.text = text
            revalidate()
            repaint()
        }
    }
}
```

### 6. Enhanced Generate Code Action

```kotlin
// GenerateCodeAction.kt
package com.yourcompany.aiassistant.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.yourcompany.aiassistant.service.APIGatewayService
import com.yourcompany.aiassistant.ui.CodeGenerationDialog

class GenerateCodeAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return
        val document = editor.document
        
        val dialog = CodeGenerationDialog(project)
        if (!dialog.showAndGet()) return
        
        val requirement = dialog.getRequirement()
        val includeContext = dialog.shouldIncludeContext()
        
        val gatewayService = APIGatewayService.getInstance()
        
        // Build file context
        val fileContext = if (includeContext) {
            val selectedText = editor.selectionModel.selectedText
            val cursorOffset = editor.caretModel.offset
            
            APIGatewayService.FileContext(
                fileName = editor.virtualFile?.name,
                fileType = editor.virtualFile?.extension,
                currentContent = document.text,
                cursorPosition = cursorOffset,
                selectedText = selectedText
            )
        } else {
            null
        }
        
        val projectContext = APIGatewayService.ProjectContext(
            projectName = project.name,
            projectPath = project.basePath
        )
        
        val request = APIGatewayService.GatewayRequest(
            prompt = "Generate code for: $requirement",
            context = APIGatewayService.RequestContext(
                fileContext = fileContext,
                projectContext = projectContext
            )
        )
        
        // Show progress
        val progressDialog = createProgressDialog(project)
        progressDialog.isVisible = true
        
        gatewayService.sendRequest(
            request = request,
            onSuccess = { response ->
                ApplicationManager.getApplication().invokeLater {
                    progressDialog.dispose()
                    
                    if (response.requiresInput && response.question != null) {
                        handleFollowUpQuestion(project, editor, requirement, response.question)
                    } else if (response.content != null) {
                        showCodePreviewAndInsert(project, editor, response.content)
                    }
                }
            },
            onError = { error ->
                ApplicationManager.getApplication().invokeLater {
                    progressDialog.dispose()
                    Messages.showErrorDialog(project, error, "Generation Failed")
                }
            }
        )
    }
    
    private fun handleFollowUpQuestion(
        project: Project,
        editor: Editor,
        originalRequirement: String,
        question: String
    ) {
        val answer = Messages.showInputDialog(
            project,
            question,
            "AI Needs More Information",
            Messages.getQuestionIcon()
        ) ?: return
        
        val gatewayService = APIGatewayService.getInstance()
        
        val request = APIGatewayService.GatewayRequest(
            prompt = originalRequirement,
            context = APIGatewayService.RequestContext(
                conversationHistory = listOf(
                    APIGatewayService.Message("user", originalRequirement),
                    APIGatewayService.Message("assistant", question),
                    APIGatewayService.Message("user", answer)
                )
            )
        )
        
        gatewayService.sendRequest(
            request = request,
            onSuccess = { response ->
                ApplicationManager.getApplication().invokeLater {
                    if (response.content != null) {
                        showCodePreviewAndInsert(project, editor, response.content)
                    }
                }
            },
            onError = { error ->
                ApplicationManager.getApplication().invokeLater {
                    Messages.showErrorDialog(project, error, "Generation Failed")
                }
            }
        )
    }
    
    private fun showCodePreviewAndInsert(project: Project, editor: Editor, code: String) {
        val result = Messages.showYesNoDialog(
            project,
            "Preview:\n\n$code\n\nInsert this code?",
            "Code Preview",
            "Insert",
            "Cancel",
            Messages.getQuestionIcon()
        )
        
        if (result == Messages.YES) {
            insertCode(project, editor, code)
        }
    }
    
    private fun insertCode(project: Project, editor: Editor, code: String) {
        WriteCommandAction.runWriteCommandAction(project) {
            val document = editor.document
            val offset = editor.caretModel.offset
            document.insertString(offset, code)
            editor.caretModel.moveToOffset(offset + code.length)
        }
    }
    
    private fun createProgressDialog(project: Project): JDialog {
        val dialog = JDialog()
        dialog.title = "Generating Code..."
        dialog.isModal = false
        dialog.defaultCloseOperation = JDialog.DO_NOTHING_ON_CLOSE
        
        val panel = JPanel(BorderLayout(10, 10))
        panel.border = BorderFactory.createEmptyBorder(20, 20, 20, 20)
        panel.add(JLabel("Please wait while AI generates your code..."), BorderLayout.CENTER)
        
        val progressBar = JProgressBar()
        progressBar.isIndeterminate = true
        panel.add(progressBar, BorderLayout.SOUTH)
        
        dialog.contentPane = panel
        dialog.pack()
        dialog.setLocationRelativeTo(null)
        
        return dialog
    }
}
```

### 7. Code Generation Dialog

```kotlin
// CodeGenerationDialog.kt
package com.yourcompany.aiassistant.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.FormBuilder
import javax.swing.JComponent
import javax.swing.JLabel

class CodeGenerationDialog(project: Project) : DialogWrapper(project) {
    
    private val requirementArea = JBTextArea(10, 50)
    private val includeContextCheckbox = JBCheckBox("Include current file context", true)
    
    init {
        title = "Generate Code with AI"
        init()
    }
    
    override fun createCenterPanel(): JComponent {
        requirementArea.lineWrap = true
        requirementArea.wrapStyleWord = true
        
        return FormBuilder.createFormBuilder()
            .addComponent(JLabel("Describe what code you want to generate:"))
            .addComponentFillVertically(requirementArea, 0)
            .addComponent(includeContextCheckbox)
            .panel
    }
    
    fun getRequirement(): String = requirementArea.text.trim()
    
    fun shouldIncludeContext(): Boolean = includeContextCheckbox.isSelected
}
```

This enhanced plugin now provides:

1. **Robust API Gateway Integration** - Direct communication with your REST API gateway
2. **Comprehensive Settings** - Gateway URL, authentication, timeouts, retries, custom headers
3. **Connection Testing** - Test your gateway connection before using
4. **Streaming Support** - Real-time response streaming if your gateway supports it
5. **Retry Logic** - Automatic retries with exponential backoff
6. **Rich Context** - Sends file, project, and conversation context
7. **Interactive UI** - Chat bubbles, timestamps, icons, error handling
8. **Progress Indicators** - Shows when AI is thinking/generating
9. **Code Preview** - Preview generated code before insertion
10. **Debug Logging** - Optional request/response logging

The gateway service expects your API to accept requests like:

```json
{
  "prompt": "user request",
  "context": {
    "conversationHistory": [...],
    "fileContext": {...},
    "projectContext": {...}
  },
  "parameters": {
    "maxTokens": 4000,
    "temperature": 0.7,
    "stream": false
  }
}
```

And return responses like:

```json
{
  "success": true,
  "content": "generated code or response",
  "question": "optional follow-up question",
  "requiresInput": false,
  "metadata": {
    "tokensUsed": 150,
    "processingTime": 2500
  }
}
```

You can customize these formats to match your actual gateway API!

# More

# Enhanced AI Code Assistant with File Generation & Bug Fixing

I'll enhance the plugin to support automatic file/project generation and compile error analysis with bug fixing.

## Additional Files & Enhancements

### 1. Enhanced `plugin.xml`

```xml
<idea-plugin>
    <id>com.yourcompany.aiassistant</id>
    <name>AI Code Assistant Pro</name>
    <vendor>Your Company</vendor>
    
    <description><![CDATA[
        Advanced AI-powered code assistant with:
        - Intelligent code generation and completion
        - Automatic file and project structure creation
        - Compile error analysis and auto-fixing
        - Runtime error debugging assistance
        - Code refactoring suggestions
        - Real-time code review
    ]]></description>
    
    <depends>com.intellij.modules.platform</depends>
    <depends>com.intellij.modules.java</depends>
    
    <extensions defaultExtensionNs="com.intellij">
        <!-- Settings -->
        <applicationConfigurable 
            instance="com.yourcompany.aiassistant.settings.AIAssistantConfigurable"/>
        <applicationService 
            serviceImplementation="com.yourcompany.aiassistant.settings.AIAssistantSettings"/>
        
        <!-- Tool Window -->
        <toolWindow 
            id="AI Assistant" 
            icon="/icons/ai_assistant.png" 
            anchor="right" 
            factoryClass="com.yourcompany.aiassistant.ui.AIAssistantToolWindowFactory"/>
        
        <!-- Project Service -->
        <projectService 
            serviceImplementation="com.yourcompany.aiassistant.service.ProjectAnalysisService"/>
        <projectService 
            serviceImplementation="com.yourcompany.aiassistant.service.ErrorAnalysisService"/>
        <projectService 
            serviceImplementation="com.yourcompany.aiassistant.service.FileGenerationService"/>
        
        <!-- Inspections -->
        <localInspection
            language="JAVA"
            shortName="AIBugDetection"
            displayName="AI Bug Detection"
            groupName="AI Assistant"
            enabledByDefault="true"
            level="WARNING"
            implementationClass="com.yourcompany.aiassistant.inspection.AIBugInspection"/>
        
        <!-- Error Handler -->
        <errorHandler 
            implementation="com.yourcompany.aiassistant.error.AIErrorReporter"/>
        
        <!-- Intention Actions -->
        <intentionAction>
            <className>com.yourcompany.aiassistant.intentions.FixWithAIIntention</className>
            <category>AI Assistant</category>
        </intentionAction>
        
        <intentionAction>
            <className>com.yourcompany.aiassistant.intentions.RefactorWithAIIntention</className>
            <category>AI Assistant</category>
        </intentionAction>
    </extensions>
    
    <actions>
        <!-- Main Menu -->
        <group id="AIAssistant.MainMenu" text="AI Assistant" popup="true">
            <add-to-group group-id="MainMenu" anchor="last"/>
            
            <action id="AIAssistant.GenerateCode" 
                    class="com.yourcompany.aiassistant.actions.GenerateCodeAction"
                    text="Generate Code"
                    description="Generate code using AI">
                <keyboard-shortcut keymap="$default" first-keystroke="ctrl alt G"/>
            </action>
            
            <action id="AIAssistant.GenerateProject"
                    class="com.yourcompany.aiassistant.actions.GenerateProjectAction"
                    text="Generate Project Structure"
                    description="Generate entire project structure">
                <keyboard-shortcut keymap="$default" first-keystroke="ctrl alt shift G"/>
            </action>
            
            <action id="AIAssistant.AnalyzeErrors"
                    class="com.yourcompany.aiassistant.actions.AnalyzeCompileErrorsAction"
                    text="Analyze &amp; Fix Compile Errors"
                    description="Analyze compilation errors and suggest fixes">
                <keyboard-shortcut keymap="$default" first-keystroke="ctrl alt F"/>
            </action>
            
            <action id="AIAssistant.FixBug"
                    class="com.yourcompany.aiassistant.actions.FixBugAction"
                    text="Fix Bug with AI"
                    description="Let AI analyze and fix the bug">
                <keyboard-shortcut keymap="$default" first-keystroke="ctrl alt B"/>
            </action>
            
            <action id="AIAssistant.GenerateTests"
                    class="com.yourcompany.aiassistant.actions.GenerateTestsAction"
                    text="Generate Unit Tests"
                    description="Generate unit tests for selected code">
            </action>
            
            <action id="AIAssistant.ExplainCode"
                    class="com.yourcompany.aiassistant.actions.ExplainCodeAction"
                    text="Explain Code"
                    description="Get AI explanation of selected code">
            </action>
        </group>
        
        <!-- Editor Context Menu -->
        <group id="AIAssistant.EditorPopup">
            <add-to-group group-id="EditorPopupMenu" anchor="last"/>
            <separator/>
            <reference ref="AIAssistant.GenerateCode"/>
            <reference ref="AIAssistant.FixBug"/>
            <reference ref="AIAssistant.GenerateTests"/>
            <reference ref="AIAssistant.ExplainCode"/>
        </group>
        
        <!-- Project View Context Menu -->
        <group id="AIAssistant.ProjectViewPopup">
            <add-to-group group-id="ProjectViewPopupMenu" anchor="last"/>
            <separator/>
            <reference ref="AIAssistant.GenerateProject"/>
        </group>
    </actions>
</idea-plugin>
```

### 2. File Generation Service

```kotlin
// FileGenerationService.kt
package com.yourcompany.aiassistant.service

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.PsiManager
import java.io.IOException

class FileGenerationService(private val project: Project) {
    
    data class FileToGenerate(
        val path: String,
        val content: String,
        val fileType: String = "java"
    )
    
    data class ProjectStructure(
        val projectName: String,
        val basePackage: String,
        val files: List<FileToGenerate>,
        val directories: List<String> = emptyList(),
        val dependencies: List<Dependency> = emptyList()
    )
    
    data class Dependency(
        val groupId: String,
        val artifactId: String,
        val version: String
    )
    
    fun generateFiles(
        files: List<FileToGenerate>,
        baseDirectory: VirtualFile,
        onProgress: (String) -> Unit = {},
        onComplete: (List<VirtualFile>) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        ApplicationManager.getApplication().invokeLater {
            WriteCommandAction.runWriteCommandAction(project) {
                try {
                    val createdFiles = mutableListOf<VirtualFile>()
                    
                    files.forEach { fileSpec ->
                        onProgress("Creating ${fileSpec.path}...")
                        
                        val file = createFile(
                            baseDirectory = baseDirectory,
                            relativePath = fileSpec.path,
                            content = fileSpec.content,
                            fileType = fileSpec.fileType
                        )
                        
                        if (file != null) {
                            createdFiles.add(file)
                        }
                    }
                    
                    onProgress("Refreshing project...")
                    baseDirectory.refresh(false, true)
                    
                    onComplete(createdFiles)
                } catch (e: Exception) {
                    onError("Failed to generate files: ${e.message}")
                }
            }
        }
    }
    
    fun generateProject(
        structure: ProjectStructure,
        parentDirectory: VirtualFile,
        onProgress: (String) -> Unit = {},
        onComplete: (VirtualFile) -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        ApplicationManager.getApplication().invokeLater {
            WriteCommandAction.runWriteCommandAction(project) {
                try {
                    // Create project root directory
                    onProgress("Creating project structure...")
                    val projectRoot = parentDirectory.createChildDirectory(this, structure.projectName)
                    
                    // Create directory structure
                    structure.directories.forEach { dir ->
                        onProgress("Creating directory: $dir")
                        createDirectory(projectRoot, dir)
                    }
                    
                    // Create standard Maven/Gradle structure if not specified
                    val srcMain = createDirectory(projectRoot, "src/main/java")
                    val srcTest = createDirectory(projectRoot, "src/test/java")
                    val resources = createDirectory(projectRoot, "src/main/resources")
                    
                    // Create package structure
                    val packagePath = structure.basePackage.replace('.', '/')
                    val mainPackage = createDirectory(srcMain, packagePath)
                    val testPackage = createDirectory(srcTest, packagePath)
                    
                    // Generate build file
                    onProgress("Generating build configuration...")
                    generateBuildFile(projectRoot, structure)
                    
                    // Generate files
                    structure.files.forEach { fileSpec ->
                        onProgress("Generating ${fileSpec.path}...")
                        
                        val targetDir = when {
                            fileSpec.path.contains("test", ignoreCase = true) -> testPackage
                            fileSpec.path.endsWith(".properties") || 
                            fileSpec.path.endsWith(".xml") || 
                            fileSpec.path.endsWith(".yml") -> resources
                            else -> mainPackage
                        }
                        
                        createFile(
                            baseDirectory = targetDir,
                            relativePath = fileSpec.path.substringAfterLast('/'),
                            content = fileSpec.content,
                            fileType = fileSpec.fileType
                        )
                    }
                    
                    // Generate README
                    onProgress("Generating README...")
                    generateReadme(projectRoot, structure)
                    
                    projectRoot.refresh(false, true)
                    onComplete(projectRoot)
                    
                } catch (e: Exception) {
                    onError("Failed to generate project: ${e.message}")
                }
            }
        }
    }
    
    private fun createFile(
        baseDirectory: VirtualFile,
        relativePath: String,
        content: String,
        fileType: String
    ): VirtualFile? {
        return try {
            val parts = relativePath.split('/')
            val fileName = parts.last()
            val dirPath = parts.dropLast(1).joinToString("/")
            
            val targetDir = if (dirPath.isNotEmpty()) {
                createDirectory(baseDirectory, dirPath)
            } else {
                baseDirectory
            }
            
            val file = targetDir.findChild(fileName) ?: targetDir.createChildData(this, fileName)
            file.setBinaryContent(content.toByteArray())
            file
        } catch (e: IOException) {
            null
        }
    }
    
    private fun createDirectory(parent: VirtualFile, path: String): VirtualFile {
        var current = parent
        path.split('/').forEach { dirName ->
            if (dirName.isNotEmpty()) {
                current = current.findChild(dirName) 
                    ?: current.createChildDirectory(this, dirName)
            }
        }
        return current
    }
    
    private fun generateBuildFile(projectRoot: VirtualFile, structure: ProjectStructure) {
        val buildGradle = """
            plugins {
                id 'java'
                id 'application'
            }
            
            group = '${structure.basePackage}'
            version = '1.0-SNAPSHOT'
            
            repositories {
                mavenCentral()
            }
            
            dependencies {
                ${structure.dependencies.joinToString("\n    ") { 
                    "implementation '${it.groupId}:${it.artifactId}:${it.version}'"
                }}
                
                testImplementation 'org.junit.jupiter:junit-jupiter:5.9.2'
                testImplementation 'org.mockito:mockito-core:5.3.1'
            }
            
            test {
                useJUnitPlatform()
            }
            
            application {
                mainClass = '${structure.basePackage}.Main'
            }
        """.trimIndent()
        
        projectRoot.createChildData(this, "build.gradle")
            .setBinaryContent(buildGradle.toByteArray())
        
        // Also create settings.gradle
        val settingsGradle = "rootProject.name = '${structure.projectName}'"
        projectRoot.createChildData(this, "settings.gradle")
            .setBinaryContent(settingsGradle.toByteArray())
    }
    
    private fun generateReadme(projectRoot: VirtualFile, structure: ProjectStructure) {
        val readme = """
            # ${structure.projectName}
            
            This project was generated by AI Code Assistant.
            
            ## Structure
            
            - Package: ${structure.basePackage}
            - Files: ${structure.files.size}
            
            ## Build
            
            ```bash
            ./gradlew build
            ```
            
            ## Run
            
            ```bash
            ./gradlew run
            ```
            
            ## Test
            
            ```bash
            ./gradlew test
            ```
        """.trimIndent()
        
        projectRoot.createChildData(this, "README.md")
            .setBinaryContent(readme.toByteArray())
    }
    
    fun openFilesInEditor(files: List<VirtualFile>) {
        ApplicationManager.getApplication().invokeLater {
            val fileEditorManager = FileEditorManager.getInstance(project)
            files.forEach { file ->
                fileEditorManager.openFile(file, true)
            }
        }
    }
    
    companion object {
        fun getInstance(project: Project): FileGenerationService {
            return project.getService(FileGenerationService::class.java)
        }
    }
}
```

### 3. Error Analysis Service

```kotlin
// ErrorAnalysisService.kt
package com.yourcompany.aiassistant.service

import com.intellij.lang.annotation.HighlightSeverity
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.compiler.CompileContext
import com.intellij.openapi.compiler.CompileStatusNotification
import com.intellij.openapi.compiler.CompilerManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import com.intellij.lang.annotation.Annotation
import com.intellij.codeInsight.daemon.impl.DaemonCodeAnalyzerEx
import com.intellij.codeInsight.daemon.impl.HighlightInfo

class ErrorAnalysisService(private val project: Project) {
    
    data class CompileError(
        val file: VirtualFile,
        val line: Int,
        val column: Int,
        val message: String,
        val severity: ErrorSeverity,
        val code: String? = null
    )
    
    enum class ErrorSeverity {
        ERROR, WARNING, INFO
    }
    
    data class ErrorAnalysis(
        val error: CompileError,
        val diagnosis: String,
        val suggestedFix: String,
        val explanation: String,
        val codeSnippet: String? = null,
        val relatedErrors: List<CompileError> = emptyList()
    )
    
    data class BugFix(
        val originalCode: String,
        val fixedCode: String,
        val explanation: String,
        val changes: List<CodeChange>
    )
    
    data class CodeChange(
        val startOffset: Int,
        val endOffset: Int,
        val oldText: String,
        val newText: String,
        val reason: String
    )
    
    fun analyzeCompileErrors(
        onComplete: (List<CompileError>) -> Unit,
        onError: (String) -> Unit
    ) {
        val compilerManager = CompilerManager.getInstance(project)
        
        compilerManager.make(object : CompileStatusNotification {
            override fun finished(aborted: Boolean, errors: Int, warnings: Int, compileContext: CompileContext) {
                ApplicationManager.getApplication().invokeLater {
                    if (aborted) {
                        onError("Compilation was aborted")
                        return@invokeLater
                    }
                    
                    val compileErrors = extractErrors(compileContext)
                    onComplete(compileErrors)
                }
            }
        })
    }
    
    private fun extractErrors(compileContext: CompileContext): List<CompileError> {
        val errors = mutableListOf<CompileError>()
        
        compileContext.getMessages(com.intellij.openapi.compiler.CompilerMessageCategory.ERROR).forEach { message ->
            val virtualFile = message.virtualFile
            if (virtualFile != null) {
                errors.add(CompileError(
                    file = virtualFile,
                    line = message.line,
                    column = message.column,
                    message = message.message,
                    severity = ErrorSeverity.ERROR
                ))
            }
        }
        
        compileContext.getMessages(com.intellij.openapi.compiler.CompilerMessageCategory.WARNING).forEach { message ->
            val virtualFile = message.virtualFile
            if (virtualFile != null) {
                errors.add(CompileError(
                    file = virtualFile,
                    line = message.line,
                    column = message.column,
                    message = message.message,
                    severity = ErrorSeverity.WARNING
                ))
            }
        }
        
        return errors
    }
    
    fun analyzeRuntimeErrors(
        stackTrace: String,
        sourceCode: String? = null
    ): String {
        // Parse stack trace and extract relevant information
        val lines = stackTrace.lines()
        val errorType = lines.firstOrNull()?.substringBefore(':')?.trim() ?: "Unknown Error"
        val errorMessage = lines.firstOrNull()?.substringAfter(':')?.trim() ?: ""
        
        val stackFrames = lines.filter { it.trim().startsWith("at ") }
            .take(5)
            .joinToString("\n")
        
        return """
            Error Type: $errorType
            Message: $errorMessage
            
            Stack Trace:
            $stackFrames
            
            Source Context:
            ${sourceCode ?: "No source code provided"}
        """.trimIndent()
    }
    
    fun getEditorErrors(editor: Editor, psiFile: PsiFile): List<CompileError> {
        val errors = mutableListOf<CompileError>()
        val document = editor.document
        
        ApplicationManager.getApplication().runReadAction {
            val daemonCodeAnalyzer = DaemonCodeAnalyzerEx.getInstanceEx(project)
            val highlightInfos = daemonCodeAnalyzer.getFileLevelHighlights(project, psiFile)
            
            highlightInfos.forEach { info ->
                if (info.severity == HighlightSeverity.ERROR || 
                    info.severity == HighlightSeverity.WARNING) {
                    
                    val line = document.getLineNumber(info.startOffset)
                    val column = info.startOffset - document.getLineStartOffset(line)
                    
                    errors.add(CompileError(
                        file = psiFile.virtualFile,
                        line = line + 1,
                        column = column + 1,
                        message = info.description,
                        severity = when (info.severity) {
                            HighlightSeverity.ERROR -> ErrorSeverity.ERROR
                            HighlightSeverity.WARNING -> ErrorSeverity.WARNING
                            else -> ErrorSeverity.INFO
                        },
                        code = document.getText(
                            com.intellij.openapi.util.TextRange(
                                info.startOffset,
                                info.endOffset
                            )
                        )
                    ))
                }
            }
        }
        
        return errors
    }
    
    fun analyzeErrorWithAI(
        error: CompileError,
        fileContent: String,
        onSuccess: (ErrorAnalysis) -> Unit,
        onError: (String) -> Unit
    ) {
        val gatewayService = APIGatewayService.getInstance()
        
        val contextLines = extractContextAroundError(fileContent, error.line, 10)
        
        val prompt = """
            Analyze this compilation error and provide a fix:
            
            File: ${error.file.name}
            Line: ${error.line}
            Column: ${error.column}
            Error: ${error.message}
            
            Code Context:
            ```
            $contextLines
            ```
            
            Please provide:
            1. Diagnosis: What's causing the error
            2. Suggested Fix: The corrected code
            3. Explanation: Why this fixes the error
            4. Related Issues: Any other potential problems in this code
            
            Format your response as JSON:
            {
                "diagnosis": "...",
                "suggestedFix": "...",
                "explanation": "...",
                "codeSnippet": "...",
                "relatedIssues": ["..."]
            }
        """.trimIndent()
        
        val request = APIGatewayService.GatewayRequest(
            prompt = prompt,
            context = APIGatewayService.RequestContext(
                fileContext = APIGatewayService.FileContext(
                    fileName = error.file.name,
                    fileType = error.file.extension,
                    currentContent = fileContent
                )
            )
        )
        
        gatewayService.sendRequest(
            request = request,
            onSuccess = { response ->
                try {
                    val analysis = parseErrorAnalysis(response.content ?: "", error)
                    onSuccess(analysis)
                } catch (e: Exception) {
                    onError("Failed to parse AI response: ${e.message}")
                }
            },
            onError = onError
        )
    }
    
    fun generateBugFix(
        buggyCode: String,
        errorDescription: String,
        context: String? = null,
        onSuccess: (BugFix) -> Unit,
        onError: (String) -> Unit
    ) {
        val gatewayService = APIGatewayService.getInstance()
        
        val prompt = """
            Fix this bug:
            
            Error/Issue: $errorDescription
            
            Buggy Code:
            ```
            $buggyCode
            ```
            
            ${if (context != null) "Additional Context:\n$context" else ""}
            
            Please provide:
            1. The fixed code
            2. Explanation of what was wrong
            3. Explanation of the fix
            4. List of specific changes made
            
            Format as JSON:
            {
                "fixedCode": "...",
                "whatWasWrong": "...",
                "howItWasFixed": "...",
                "changes": [
                    {"description": "...", "reason": "..."}
                ]
            }
        """.trimIndent()
        
        val request = APIGatewayService.GatewayRequest(prompt = prompt)
        
        gatewayService.sendRequest(
            request = request,
            onSuccess = { response ->
                try {
                    val bugFix = parseBugFix(response.content ?: "", buggyCode)
                    onSuccess(bugFix)
                } catch (e: Exception) {
                    onError("Failed to parse bug fix: ${e.message}")
                }
            },
            onError = onError
        )
    }
    
    private fun extractContextAroundError(
        content: String,
        errorLine: Int,
        contextLines: Int
    ): String {
        val lines = content.lines()
        val startLine = maxOf(0, errorLine - contextLines - 1)
        val endLine = minOf(lines.size, errorLine + contextLines)
        
        return lines.subList(startLine, endLine)
            .mapIndexed { index, line ->
                val lineNumber = startLine + index + 1
                val marker = if (lineNumber == errorLine) ">>> " else "    "
                "$marker$lineNumber: $line"
            }
            .joinToString("\n")
    }
    
    private fun parseErrorAnalysis(aiResponse: String, error: CompileError): ErrorAnalysis {
        val gson = com.google.gson.Gson()
        
        // Try to extract JSON from the response
        val jsonStart = aiResponse.indexOf('{')
        val jsonEnd = aiResponse.lastIndexOf('}') + 1
        
        val json = if (jsonStart >= 0 && jsonEnd > jsonStart) {
            aiResponse.substring(jsonStart, jsonEnd)
        } else {
            aiResponse
        }
        
        return try {
            val jsonObject = gson.fromJson(json, com.google.gson.JsonObject::class.java)
            
            ErrorAnalysis(
                error = error,
                diagnosis = jsonObject.get("diagnosis")?.asString ?: "Unknown",
                suggestedFix = jsonObject.get("suggestedFix")?.asString ?: "",
                explanation = jsonObject.get("explanation")?.asString ?: "",
                codeSnippet = jsonObject.get("codeSnippet")?.asString
            )
        } catch (e: Exception) {
            // Fallback: treat entire response as explanation
            ErrorAnalysis(
                error = error,
                diagnosis = "AI Analysis",
                suggestedFix = aiResponse,
                explanation = aiResponse
            )
        }
    }
    
    private fun parseBugFix(aiResponse: String, originalCode: String): BugFix {
        val gson = com.google.gson.Gson()
        
        val jsonStart = aiResponse.indexOf('{')
        val jsonEnd = aiResponse.lastIndexOf('}') + 1
        
        val json = if (jsonStart >= 0 && jsonEnd > jsonStart) {
            aiResponse.substring(jsonStart, jsonEnd)
        } else {
            aiResponse
        }
        
        return try {
            val jsonObject = gson.fromJson(json, com.google.gson.JsonObject::class.java)
            
            val fixedCode = jsonObject.get("fixedCode")?.asString ?: aiResponse
            val explanation = "${jsonObject.get("whatWasWrong")?.asString ?: ""}\n\n" +
                             "${jsonObject.get("howItWasFixed")?.asString ?: ""}"
            
            val changes = mutableListOf<CodeChange>()
            jsonObject.get("changes")?.asJsonArray?.forEach { change ->
                val changeObj = change.asJsonObject
                changes.add(CodeChange(
                    startOffset = 0,
                    endOffset = originalCode.length,
                    oldText = originalCode,
                    newText = fixedCode,
                    reason = changeObj.get("description")?.asString ?: ""
                ))
            }
            
            BugFix(
                originalCode = originalCode,
                fixedCode = fixedCode,
                explanation = explanation,
                changes = changes
            )
        } catch (e: Exception) {
            BugFix(
                originalCode = originalCode,
                fixedCode = aiResponse,
                explanation = "AI-generated fix",
                changes = emptyList()
            )
        }
    }
    
    companion object {
        fun getInstance(project: Project): ErrorAnalysisService {
            return project.getService(ErrorAnalysisService::class.java)
        }
    }
}
```

### 4. Generate Project Action

```kotlin
// GenerateProjectAction.kt
package com.yourcompany.aiassistant.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.yourcompany.aiassistant.service.APIGatewayService
import com.yourcompany.aiassistant.service.FileGenerationService
import com.yourcompany.aiassistant.ui.ProjectGenerationDialog
import com.google.gson.Gson

class GenerateProjectAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val selectedDir = e.getData(CommonDataKeys.VIRTUAL_FILE) ?: project.baseDir ?: return
        
        val dialog = ProjectGenerationDialog(project)
        if (!dialog.showAndGet()) return
        
        val requirement = dialog.getRequirement()
        val projectType = dialog.getProjectType()
        
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            "Generating Project with AI",
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Asking AI to design project structure..."
                indicator.isIndeterminate = false
                indicator.fraction = 0.1
                
                val gatewayService = APIGatewayService.getInstance()
                
                val prompt = """
                    Generate a complete ${projectType} project structure for:
                    
                    Requirements: $requirement
                    
                    Please provide a JSON response with this structure:
                    {
                        "projectName": "project-name",
                        "basePackage": "com.example.project",
                        "directories": ["src/main/java", "src/main/resources", ...],
                        "files": [
                            {
                                "path": "com/example/Main.java",
                                "content": "package com.example;...",
                                "fileType": "java"
                            }
                        ],
                        "dependencies": [
                            {
                                "groupId": "org.springframework.boot",
                                "artifactId": "spring-boot-starter-web",
                                "version": "3.2.0"
                            }
                        ]
                    }
                    
                    Include:
                    - Main application class
                    - Configuration files
                    - Example service/controller classes
                    - Unit test examples
                    - README.md
                    - Build configuration (Gradle)
                """.trimIndent()
                
                val request = APIGatewayService.GatewayRequest(
                    prompt = prompt,
                    context = APIGatewayService.RequestContext(
                        projectContext = APIGatewayService.ProjectContext(
                            projectName = project.name,
                            projectPath = project.basePath,
                            projectType = projectType
                        )
                    )
                )
                
                var projectGenerated = false
                
                gatewayService.sendRequest(
                    request = request,
                    onSuccess = { response ->
                        indicator.fraction = 0.5
                        indicator.text = "Parsing project structure..."
                        
                        try {
                            val structure = parseProjectStructure(response.content ?: "")
                            
                            indicator.fraction = 0.6
                            indicator.text = "Creating files and directories..."
                            
                            val fileService = FileGenerationService.getInstance(project)
                            
                            fileService.generateProject(
                                structure = structure,
                                parentDirectory = selectedDir,
                                onProgress = { message ->
                                    indicator.text = message
                                },
                                onComplete = { projectRoot ->
                                    indicator.fraction = 1.0
                                    projectGenerated = true
                                    
                                    ApplicationManager.getApplication().invokeLater {
                                        val result = Messages.showYesNoDialog(
                                            project,
                                            "Project '${structure.projectName}' created successfully!\n\n" +
                                            "Location: ${projectRoot.path}\n\n" +
                                            "Would you like to open it in a new window?",
                                            "Project Generated",
                                            "Open Project",
                                            "Close",
                                            Messages.getInformationIcon()
                                        )
                                        
                                        if (result == Messages.YES) {
                                            openProject(projectRoot.path)
                                        }
                                    }
                                },
                                onError = { error ->
                                    showError(project, error)
                                }
                            )
                        } catch (e: Exception) {
                            showError(project, "Failed to parse project structure: ${e.message}")
                        }
                    },
                    onError = { error ->
                        showError(project, error)
                    }
                )
                
                // Wait for completion
                while (!projectGenerated && !indicator.isCanceled) {
                    Thread.sleep(100)
                }
            }
        })
    }
    
    private fun parseProjectStructure(jsonResponse: String): FileGenerationService.ProjectStructure {
        val gson = Gson()
        
        // Extract JSON from response if it's embedded in text
        val jsonStart = jsonResponse.indexOf('{')
        val jsonEnd = jsonResponse.lastIndexOf('}') + 1
        
        val json = if (jsonStart >= 0 && jsonEnd > jsonStart) {
            jsonResponse.substring(jsonStart, jsonEnd)
        } else {
            jsonResponse
        }
        
        val jsonObject = gson.fromJson(json, com.google.gson.JsonObject::class.java)
        
        val files = jsonObject.getAsJsonArray("files").map { fileElement ->
            val fileObj = fileElement.asJsonObject
            FileGenerationService.FileToGenerate(
                path = fileObj.get("path").asString,
                content = fileObj.get("content").asString,
                fileType = fileObj.get("fileType")?.asString ?: "java"
            )
        }
        
        val dependencies = jsonObject.getAsJsonArray("dependencies")?.map { depElement ->
            val depObj = depElement.asJsonObject
            FileGenerationService.Dependency(
                groupId = depObj.get("groupId").asString,
                artifactId = depObj.get("artifactId").asString,
                version = depObj.get("version").asString
            )
        } ?: emptyList()
        
        val directories = jsonObject.getAsJsonArray("directories")?.map {
            it.asString
        } ?: emptyList()
        
        return FileGenerationService.ProjectStructure(
            projectName = jsonObject.get("projectName").asString,
            basePackage = jsonObject.get("basePackage").asString,
            files = files,
            directories = directories,
            dependencies = dependencies
        )
    }
    
    private fun openProject(path: String) {
        // Open project in new window
        val projectManager = com.intellij.openapi.project.ex.ProjectManagerEx.getInstanceEx()
        projectManager.openProject(java.nio.file.Paths.get(path), com.intellij.openapi.project.OpenProjectTask())
    }
    
    private fun showError(project: com.intellij.openapi.project.Project, message: String) {
        ApplicationManager.getApplication().invokeLater {
            Messages.showErrorDialog(project, message, "Generation Failed")
        }
    }
}
```

### 5. Project Generation Dialog

```kotlin
// ProjectGenerationDialog.kt
package com.yourcompany.aiassistant.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.FormBuilder
import javax.swing.*

class ProjectGenerationDialog(project: Project) : DialogWrapper(project) {
    
    private val requirementArea = JBTextArea(10, 50)
    private val projectTypeCombo = JComboBox(arrayOf(
        "Java Spring Boot Web Application",
        "Java Console Application",
        "Java Microservice",
        "Java REST API",
        "Java Library/SDK",
        "Kotlin Application",
        "Gradle Multi-Module Project",
        "Maven Multi-Module Project"
    ))
    
    init {
        title = "Generate Project with AI"
        init()
    }
    
    override fun createCenterPanel(): JComponent {
        requirementArea.lineWrap = true
        requirementArea.wrapStyleWord = true
        requirementArea.text = "Example: Create a REST API for managing a todo list with CRUD operations, " +
                "authentication, and PostgreSQL database"
        
        return FormBuilder.createFormBuilder()
            .addLabeledComponent(JLabel("Project Type:"), projectTypeCombo)
            .addVerticalGap(10)
            .addComponent(JLabel("Describe your project requirements in detail:"))
            .addComponentFillVertically(requirementArea, 0)
            .addComponent(JLabel("<html><i>Tip: Be specific about features, technologies, and structure you want</i></html>"))
            .panel
    }
    
    fun getRequirement(): String = requirementArea.text.trim()
    
    fun getProjectType(): String = projectTypeCombo.selectedItem as String
}
```

### 6. Analyze & Fix Compile Errors Action

```kotlin
// AnalyzeCompileErrorsAction.kt
package com.yourcompany.aiassistant.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.yourcompany.aiassistant.service.ErrorAnalysisService
import com.yourcompany.aiassistant.ui.ErrorFixDialog

class AnalyzeCompileErrorsAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val errorService = ErrorAnalysisService.getInstance(project)
        
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            "Analyzing Compile Errors",
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "Compiling project and collecting errors..."
                indicator.isIndeterminate = true
                
                errorService.analyzeCompileErrors(
                    onComplete = { errors ->
                        if (errors.isEmpty()) {
                            ApplicationManager.getApplication().invokeLater {
                                Messages.showInfoMessage(
                                    project,
                                    "No compilation errors found!",
                                    "Success"
                                )
                            }
                            return@analyzeCompileErrors
                        }
                        
                        indicator.text = "Analyzing ${errors.size} error(s) with AI..."
                        
                        ApplicationManager.getApplication().invokeLater {
                            val dialog = ErrorFixDialog(project, errors)
                            dialog.show()
                        }
                    },
                    onError = { error ->
                        ApplicationManager.getApplication().invokeLater {
                            Messages.showErrorDialog(project, error, "Analysis Failed")
                        }
                    }
                )
            }
        })
    }
}
```

### 7. Fix Bug Action

```kotlin
// FixBugAction.kt
package com.yourcompany.aiassistant.actions

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.ui.Messages
import com.yourcompany.aiassistant.service.ErrorAnalysisService
import com.yourcompany.aiassistant.ui.BugFixPreviewDialog

class FixBugAction : AnAction() {
    
    override fun actionPerformed(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val project = e.project ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        
        val selectedText = editor.selectionModel.selectedText
        val bugDescription = Messages.showInputDialog(
            project,
            "Describe the bug or issue:",
            "Fix Bug with AI",
            Messages.getQuestionIcon()
        ) ?: return
        
        val code = selectedText ?: editor.document.text
        
        ProgressManager.getInstance().run(object : Task.Backgroundable(
            project,
            "Analyzing and Fixing Bug",
            true
        ) {
            override fun run(indicator: ProgressIndicator) {
                indicator.text = "AI is analyzing the bug..."
                indicator.isIndeterminate = true
                
                val errorService = ErrorAnalysisService.getInstance(project)
                
                // Get any visible errors
                val editorErrors = errorService.getEditorErrors(editor, psiFile)
                val context = if (editorErrors.isNotEmpty()) {
                    "Current errors in file:\n" + editorErrors.joinToString("\n") {
                        "Line ${it.line}: ${it.message}"
                    }
                } else {
                    null
                }
                
                errorService.generateBugFix(
                    buggyCode = code,
                    errorDescription = bugDescription,
                    context = context,
                    onSuccess = { bugFix ->
                        ApplicationManager.getApplication().invokeLater {
                            val dialog = BugFixPreviewDialog(project, bugFix)
                            if (dialog.showAndGet()) {
                                applyFix(editor, bugFix, selectedText != null)
                            }
                        }
                    },
                    onError = { error ->
                        ApplicationManager.getApplication().invokeLater {
                            Messages.showErrorDialog(project, error, "Bug Fix Failed")
                        }
                    }
                )
            }
        })
    }
    
    private fun applyFix(editor: Editor, bugFix: ErrorAnalysisService.BugFix, isSelection: Boolean) {
        WriteCommandAction.runWriteCommandAction(editor.project) {
            val document = editor.document
            
            if (isSelection) {
                val selectionStart = editor.selectionModel.selectionStart
                val selectionEnd = editor.selectionModel.selectionEnd
                document.replaceString(selectionStart, selectionEnd, bugFix.fixedCode)
            } else {
                document.setText(bugFix.fixedCode)
            }
        }
    }
    
    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = editor != null
    }
}
```

### 8. Error Fix Dialog

```kotlin
// ErrorFixDialog.kt
package com.yourcompany.aiassistant.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.yourcompany.aiassistant.service.ErrorAnalysisService
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class ErrorFixDialog(
    private val project: Project,
    private val errors: List<ErrorAnalysisService.CompileError>
) : DialogWrapper(project) {
    
    private val errorList = JBList(errors.map { 
        "${it.file.name}:${it.line} - ${it.message}"
    })
    private val diagnosisArea = JBTextArea()
    private val fixArea = JBTextArea()
    private val explanationArea = JBTextArea()
    
    private var currentAnalysis: ErrorAnalysisService.ErrorAnalysis? = null
    
    init {
        title = "Compile Errors Analysis & Fixes"
        init()
        setupListeners()
        
        if (errors.isNotEmpty()) {
            errorList.selectedIndex = 0
        }
    }
    
    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(10, 10))
        panel.preferredSize = Dimension(900, 600)
        
        // Left panel - error list
        val leftPanel = JPanel(BorderLayout()).apply {
            preferredSize = Dimension(300, 600)
            add(JLabel("Errors (${errors.size}):"), BorderLayout.NORTH)
            add(JBScrollPane(errorList), BorderLayout.CENTER)
            
            val analyzeButton = JButton("Analyze Selected")
            analyzeButton.addActionListener { analyzeSelectedError() }
            add(analyzeButton, BorderLayout.SOUTH)
        }
        
        // Right panel - analysis and fix
        val rightPanel = JPanel(BorderLayout(5, 5))
        
        diagnosisArea.isEditable = false
        diagnosisArea.lineWrap = true
        diagnosisArea.wrapStyleWord = true
        
        fixArea.isEditable = false
        fixArea.lineWrap = true
        fixArea.wrapStyleWord = true
        fixArea.font = java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12)
        
        explanationArea.isEditable = false
        explanationArea.lineWrap = true
        explanationArea.wrapStyleWord = true
        
        val tabbedPane = JTabbedPane().apply {
            addTab("Diagnosis", JBScrollPane(diagnosisArea))
            addTab("Suggested Fix", JBScrollPane(fixArea))
            addTab("Explanation", JBScrollPane(explanationArea))
        }
        
        rightPanel.add(tabbedPane, BorderLayout.CENTER)
        
        val applyButton = JButton("Apply Fix")
        applyButton.addActionListener { applyCurrentFix() }
        rightPanel.add(applyButton, BorderLayout.SOUTH)
        
        // Split pane
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, rightPanel)
        splitPane.dividerLocation = 300
        
        panel.add(splitPane, BorderLayout.CENTER)
        
        return panel
    }
    
    private fun setupListeners() {
        errorList.addListSelectionListener {
            if (!it.valueIsAdjusting) {
                analyzeSelectedError()
            }
        }
    }
    
    private fun analyzeSelectedError() {
        val selectedIndex = errorList.selectedIndex
        if (selectedIndex < 0 || selectedIndex >= errors.size) return
        
        val error = errors[selectedIndex]
        
        diagnosisArea.text = "Analyzing..."
        fixArea.text = ""
        explanationArea.text = ""
        
        val errorService = ErrorAnalysisService.getInstance(project)
        
        ApplicationManager.getApplication().runReadAction {
            val fileContent = String(error.file.contentsToByteArray())
            
            errorService.analyzeErrorWithAI(
                error = error,
                fileContent = fileContent,
                onSuccess = { analysis ->
                    currentAnalysis = analysis
                    
                    ApplicationManager.getApplication().invokeLater {
                        diagnosisArea.text = analysis.diagnosis
                        fixArea.text = analysis.codeSnippet ?: analysis.suggestedFix
                        explanationArea.text = analysis.explanation
                    }
                },
                onError = { errorMsg ->
                    ApplicationManager.getApplication().invokeLater {
                        diagnosisArea.text = "Error: $errorMsg"
                    }
                }
            )
        }
    }
    
    private fun applyCurrentFix() {
        val analysis = currentAnalysis ?: return
        
        val result = Messages.showYesNoDialog(
            project,
            "Apply this fix to ${analysis.error.file.name}?",
            "Confirm Fix",
            Messages.getQuestionIcon()
        )
        
        if (result == Messages.YES) {
            WriteCommandAction.runWriteCommandAction(project) {
                try {
                    val file = analysis.error.file
                    val document = com.intellij.openapi.fileEditor.FileDocumentManager
                        .getInstance()
                        .getDocument(file)
                    
                    if (document != null && analysis.codeSnippet != null) {
                        val startOffset = document.getLineStartOffset(analysis.error.line - 1)
                        val endOffset = document.getLineEndOffset(analysis.error.line - 1)
                        
                        document.replaceString(startOffset, endOffset, analysis.codeSnippet)
                        
                        // Open the file
                        FileEditorManager.getInstance(project).openFile(file, true)
                        
                        Messages.showInfoMessage(project, "Fix applied successfully!", "Success")
                    }
                } catch (e: Exception) {
                    Messages.showErrorDialog(project, "Failed to apply fix: ${e.message}", "Error")
                }
            }
        }
    }
}
```

### 9. Bug Fix Preview Dialog

```kotlin
// BugFixPreviewDialog.kt
package com.yourcompany.aiassistant.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.yourcompany.aiassistant.service.ErrorAnalysisService
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.*

class BugFixPreviewDialog(
    project: Project,
    private val bugFix: ErrorAnalysisService.BugFix
) : DialogWrapper(project) {
    
    init {
        title = "Bug Fix Preview"
        init()
    }
    
    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(10, 10))
        panel.preferredSize = Dimension(800, 600)
        
        // Explanation at top
        val explanationArea = JBTextArea(bugFix.explanation)
        explanationArea.isEditable = false
        explanationArea.lineWrap = true
        explanationArea.wrapStyleWord = true
        val explanationPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createTitledBorder("Explanation")
            add(JBScrollPane(explanationArea), BorderLayout.CENTER)
            preferredSize = Dimension(800, 150)
        }
        
        // Split pane with before/after
        val beforeArea = JBTextArea(bugFix.originalCode)
        beforeArea.isEditable = false
        beforeArea.font = java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12)
        
        val afterArea = JBTextArea(bugFix.fixedCode)
        afterArea.isEditable = false
        afterArea.font = java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12)
        
        val beforePanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createTitledBorder("Before (Original Code)")
            add(JBScrollPane(beforeArea), BorderLayout.CENTER)
        }
        
        val afterPanel = JPanel(BorderLayout()).apply {
            border = BorderFactory.createTitledBorder("After (Fixed Code)")
            add(JBScrollPane(afterArea), BorderLayout.CENTER)
        }
        
        val splitPane = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, beforePanel, afterPanel)
        splitPane.dividerLocation = 400
        
        panel.add(explanationPanel, BorderLayout.NORTH)
        panel.add(splitPane, BorderLayout.CENTER)
        
        // Changes list
        if (bugFix.changes.isNotEmpty()) {
            val changesList = JList(bugFix.changes.map { it.reason }.toTypedArray())
            val changesPanel = JPanel(BorderLayout()).apply {
                border = BorderFactory.createTitledBorder("Changes Made")
                add(JBScrollPane(changesList), BorderLayout.CENTER)
                preferredSize = Dimension(800, 100)
            }
            panel.add(changesPanel, BorderLayout.SOUTH)
        }
        
        return panel
    }
    
    override fun createActions(): Array<Action> {
        return arrayOf(okAction, cancelAction)
    }
    
    init {
        okAction.putValue(Action.NAME, "Apply Fix")
        cancelAction.putValue(Action.NAME, "Cancel")
    }
}
```

This enhanced version now supports:

1. **Automatic Project Generation** - Creates entire project structures with multiple files
2. **Compile Error Analysis** - Detects and analyzes compilation errors
3. **Bug Fixing** - AI-powered bug detection and fixing
4. **File Creation** - Automatically creates files with proper directory structure
5. **Code Context** - Sends file and project context to AI
6. **Visual Diffs** - Shows before/after code comparisons
7. **Error Explanations** - Detailed explanations of what went wrong and how it was fixed
8. **Batch Error Fixing** - Can analyze and fix multiple errors
9. **Progress Tracking** - Shows progress during long operations
10. **Build File Generation** - Creates Gradle/Maven configurations

The AI gateway should handle prompts for project generation and return structured JSON responses!

