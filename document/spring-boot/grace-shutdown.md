# shutdown wait

When using `systemd` to manage a Spring Boot service, you can configure it to wait for the shutdown process to complete before restarting the application. By default, `systemd` sends a `SIGTERM` signal to stop the service and waits for it to terminate. However, if the shutdown process takes longer (e.g., 15 seconds), you need to ensure that `systemd` waits long enough for your application's shutdown hooks to finish.

Here’s how you can configure `systemd` to handle this:

---

### **1. Configure `TimeoutStopSec` in the Systemd Service File**
The `TimeoutStopSec` directive specifies how long `systemd` will wait for the service to shut down gracefully before forcibly killing it with a `SIGKILL` signal. Increase this timeout to accommodate your shutdown process (e.g., 15 seconds).

#### Example Systemd Service File
Create or update your service file (e.g., `/etc/systemd/system/myapp.service`):

```ini
[Unit]
Description=My Spring Boot Application
After=network.target

[Service]
User=myuser
Group=mygroup
ExecStart=/usr/bin/java -jar /path/to/your-app.jar
SuccessExitStatus=143
TimeoutStopSec=30
Restart=always

[Install]
WantedBy=multi-user.target
```

- **`TimeoutStopSec=30`**: This ensures `systemd` will wait up to 30 seconds for your application to shut down before sending a `SIGKILL`.
- **`SuccessExitStatus=143`**: This treats exit code `143` (caused by a `SIGTERM`) as a successful exit, so `systemd` won't treat the shutdown as a failure.
- **`Restart=always`**: Ensures the application restarts after shutdown.

After updating the service file, reload the `systemd` configuration:

```bash
sudo systemctl daemon-reload
```

To apply changes:

```bash
sudo systemctl restart myapp.service
```

---

### **2. Ensure Your Application Handles `SIGTERM` Gracefully**
Spring Boot applications handle `SIGTERM` gracefully by default. When `systemd` sends a `SIGTERM` signal to the process, Spring Boot triggers the shutdown process, including lifecycle hooks like `@PreDestroy`, `DisposableBean.destroy()`, or `SmartLifecycle.stop()`.

If your shutdown logic takes time (e.g., 15 seconds), ensure you have implemented it properly and tested it. For example:

```java
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class CustomShutdownHook {

    @PreDestroy
    public void onShutdown() throws InterruptedException {
        System.out.println("Performing custom shutdown logic...");
        Thread.sleep(15000); // Simulate a delay (e.g., releasing resources)
        System.out.println("Shutdown logic completed!");
    }
}
```

---

### **3. Verify the Shutdown Behavior**
1. Start your application using `systemctl`:
   ```bash
   sudo systemctl start myapp.service
   ```

2. Restart the application:
   ```bash
   sudo systemctl restart myapp.service
   ```

3. During the restart, check the logs to ensure your shutdown logic executes completely before the application restarts:
   ```bash
   journalctl -u myapp.service -f
   ```

You should see the logs from your shutdown hooks (e.g., `@PreDestroy` or `DisposableBean.destroy()`) before the service restarts.

---

### **4. Optional: Debug/Simulate Long Shutdowns**
If you'd like to simulate a long shutdown process for testing, you can add a delay in your shutdown logic (as shown in the example above). This will help you verify that `systemd` waits for the shutdown to complete.

You can also use `systemctl status` to monitor the service state during shutdown:

```bash
sudo systemctl status myapp.service
```

---

### **5. Handle Edge Cases**
- **If the Shutdown Takes Too Long:** If the shutdown exceeds the `TimeoutStopSec` value, `systemd` will forcefully terminate the process with `SIGKILL`. Ensure your timeout is long enough to handle the worst-case scenario.

- **Force Restart After Timeout:** If your process doesn't exit gracefully within the timeout, you can still force a restart using `RestartForceExitStatus` in the service file:
  ```ini
  RestartForceExitStatus=SIGKILL
  ```

---

### **6. Example Workflow for Restart**
When you run `sudo systemctl restart myapp.service`, the following happens:
1. `systemd` sends a `SIGTERM` signal to the application.
2. Spring Boot begins its graceful shutdown process (e.g., running `@PreDestroy` methods).
3. `systemd` waits for the application to terminate, up to the timeout specified in `TimeoutStopSec`.
4. If the application shuts down gracefully within the timeout, `systemd` restarts it.

If the application does not shut down within the timeout, `systemd` sends a `SIGKILL` signal to forcibly terminate the process.

---

### **Summary**
To make `systemctl restart` wait for your Spring Boot application's shutdown process to finish:
1. Set `TimeoutStopSec` in your `systemd` service file to a value greater than your application's expected shutdown duration.
2. Ensure your application handles `SIGTERM` gracefully and completes shutdown hooks (e.g., `@PreDestroy`).
3. Test and verify logs to ensure everything works as expected.

This approach ensures that your shutdown logic is executed fully before the application restarts.

The signal `143` is the **exit code** received when a process is terminated by a `SIGTERM` signal. Here's the breakdown:

---

### **What Does Signal 143 Mean?**
- **Signal 143** is derived from the **standard Unix/Linux signal number for `SIGTERM`**, which is **15**.
- By convention, when a process is terminated by a signal, the exit code is calculated as:

  ```text
  exit_code = signal_number + 128
  ```

  So:
    - `SIGTERM` (signal number 15) → `exit_code = 15 + 128 = 143`.

---

### **When Does Signal 143 Occur?**
Signal 143 typically indicates that:
1. **A `SIGTERM` was sent to the process.** This is a signal requesting the process to terminate gracefully.
2. The process exited as requested (gracefully or not).

`SIGTERM` is often sent:
- When you stop a service managed by `systemd` using `systemctl stop <service>` or `systemctl restart <service>`.
- When you manually terminate a process using `kill <pid>` (default signal is `SIGTERM`).

---

### **In the Context of Spring Boot Applications**
- When Spring Boot receives `SIGTERM`, it begins its **graceful shutdown process**, which includes:
    - Running `@PreDestroy` annotated methods.
    - Invoking the `destroy()` method of beans implementing `DisposableBean`.
    - Stopping `SmartLifecycle` beans.
    - Cleaning up resources (e.g., closing database connections, thread pools, etc.).
- After completing this shutdown process, the application exits with code **143** to indicate it was terminated by `SIGTERM`.

---

### **How to Handle Exit Code 143 in Systemd**
If you are using `systemd` to manage your application, you might notice the exit code 143 in logs or status reports. To ensure `systemd` treats this exit code as normal and doesn't mark the service as failed, configure your service file to include:

```ini
SuccessExitStatus=143
```

This tells `systemd` that exit code 143 is expected and should not be treated as an error.

---

### **Summary**
- **Signal 143** means the process was terminated by a `SIGTERM` signal (signal 15).
- It’s a standard way for processes to gracefully shut down in Unix/Linux systems.
- In the case of Spring Boot, it indicates the application exited gracefully after receiving a termination signal.