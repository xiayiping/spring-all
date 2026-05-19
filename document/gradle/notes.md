
# init

```shell
# init by
gradle init

# add wrapper to existing project
gradle wrapper

# upgrade version
./gradlew wrapper --gradle-version=9.5.1

```

# Phases

- Initialization
  - figure out project to build
  - execute setting.gradle
- Configuration
  - execute all build.gradle files (which actually a config file)
  - build in-memory representation of your project
- Execution
  - figure out which tasks to execute
  - decide and execute how incrementally run the tasks

# Daemon

```shell
# list daemons
./gradlew --status
# stop daemons
./gradlew --stop
```

# Tasks

```groovy

tasks.register('copyMsg' , Copy) {
    group 'CustomerGroup'
    description 'description of copy message task'
    from 'origin.txt'
    into "$buildDir/copy-target"

//    enabled = true
    onlyIf {
        1==1
    } 
}


// locate a registered task and config it

tasks.named("copyMsg") {
    into "$buildDir/copy-target-2"
}
```

```kotlin
plugins {
    id("base")
}

tasks.register<Copy>("hello") {
    from("hello.txt")
    into("$buildDir")
    println("hello end")

    doFirst {
        println("do")
    }

    doLast {
        println("do")
    }
    
//    enabled = true
    onlyIf {
        1==1
    }
}

tasks.register("bye") {
    doLast {
        println("goodbye")
    }
}


// locate a registered task and config it

tasks.named<Copy>("hello") {
    into ("$buildDir/copy-target-2")
}


```


- tasks.named<T> returns a type of TaskProvider<T> (lazy create task)
- tasks.getByName<T>(name) returns a Task itself  (eager create task)
  - should avoid using this to locate task , always use named


## dependency

```kotlin

tasks.register("hello") {
    doLast {
        println("hello")
    }
}

tasks.register("hello2") {
    doLast {
        println("hello")
    }
    // for manage the order for tasks which are decided to run
    mustRunAfter(tasks.named("hello"))
    // always run (like finally in try-catch)
    finalizedBy(tasks.named("hello"))
}

tasks.register("echo") {
    doLast {
        println("echo")
    }
    
    dependsOn(tasks.named("hello"))
}


```

# Project

https://docs.gradle.org/current/javadoc/org/gradle/api/Project.html

method starts with set is a property key.

project is the default object in build.gradle script.

# Plugins

- plugins automatically register tasks in project
- you can execute any tasks exported by a plugin
- tasks exposed by a plugin may be pre-packaged or custom
