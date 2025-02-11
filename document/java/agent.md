# Agent using ByteBuddy

```java

public class IoSessionInterceptor {

    @Advice.OnMethodEnter
    public static long onEnter(
        @Advice.Argument(0) Object message, // First argument
        @Advice.Argument(1) SocketAddress address // Second argument
    ) {
        // Capture the current timestamp as the method starts
        return System.nanoTime();
    }

    @Advice.OnMethodExit
    public static void onExit(@Advice.Enter long startTime, @Advice.Return WriteFuture future) {
        if (future != null) {
            future.addListener(new IoFutureListenerTimer(startTime));
        }
    }

    public static class IoFutureListenerTimer implements IoFutureListener<WriteFuture> {
        final long startTime;

        public IoFutureListenerTimer(long startTime) {
            this.startTime = startTime;
        }

        @Override
        public void operationComplete(WriteFuture future) {
            long elapsedTime = System.nanoTime() - startTime; // Calculate elapsed time
            System.out.println("Time taken for write operation: " + (elapsedTime / 1_000_000.0) + " ms");
        }
    }
}

public class ByteBuddyAgent {

    public static void premain(String agentArgs, Instrumentation inst) {
        long start = System.currentTimeMillis();
        System.out.println("Starting ByteBuddy agent...");

        new AgentBuilder.Default()
            .type((type, classLoader, module, clz, protectionDomain) -> {
                    return !type.isAbstract() && type.isAssignableTo(IoSession.class) ;
                }
            ) // Target IoSessionResponder class
            .transform((builder, type, classLoader, clz, module) ->
                builder.method(
                        ElementMatchers.named("write")
                            .and(ElementMatchers.takesArguments(Object.class, SocketAddress.class))
                    ) // Return type WriteFuture
                    .intercept(Advice.to(IoSessionInterceptor.class))
            ) // Add the listener logic
            .installOn(inst);
        System.out.println("Finish ByteBuddy agent takes " + (System.currentTimeMillis() - start) + " milliseconds.");
    }
}

```

```xml
<dependency>
    <groupId>net.bytebuddy</groupId>
    <artifactId>byte-buddy</artifactId>
    <version>${version.bytebuddy}</version>
</dependency>

<build>
    <plugins>
        <plugin>
            <artifactId>maven-jar-plugin</artifactId>
            <version>3.3.0</version>
            <configuration>
                <archive>
                    <manifestEntries>
                        <Premain-Class>com.msa.quickfix.agent.ByteBuddyAgent</Premain-Class>
                    </manifestEntries>
                </archive>
            </configuration>
        </plugin>
    </plugins>
</build>

```