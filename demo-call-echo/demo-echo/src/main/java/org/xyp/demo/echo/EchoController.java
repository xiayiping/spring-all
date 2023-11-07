package org.xyp.demo.echo;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.xyp.demo.echo.pojo.User;
import org.xyp.demo.echo.service.UserRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Slf4j
@RestController
@RequestMapping("/")
@Validated
public class EchoController {

    private final UserRepository userRepository;

    private final Timer defaultTimer;

    public EchoController(MeterRegistry meterRegistry,
                          UserRepository repository) {
        this.userRepository = repository;
        defaultTimer = meterRegistry.timer("default.echo.timer");
        log.info("EchoController created");
    }

    private final Random random = new Random();

    // A TimedAspect is needed for @Timed annotation
    // See EchoMainApp
    @Timed(value = "root_path_time", description = "the time spent on root path")
    @GetMapping("/")
    public String base() {
        log.info("base from echo");
        return "base echo " + random.nextInt(100);
    }

    @GetMapping("/users")
    public String users() {
        val users = defaultTimer.record(userRepository::count);
        log.info("get users from echo total user {}", users);
        return "get users echo " + random.nextInt(100);
    }

    @GetMapping("/users/add")
    public String addUsers() {
        User user = User.builder().name("user " + System.currentTimeMillis()).build();
        val saved = userRepository.save(user);
        log.info("add user {}", saved);
        return "get users echo " + saved;
    }

    @Timed("echo_timer")
    @GetMapping("/echo")
    public String call() {
        log.info("echo from echo");
        return "echo " + random.nextInt(100);
    }

    @GetMapping("/nullcheck")
    public String check(@NotNull String title) {
        return "hello " + title;
    }

    @GetMapping("/fileService")
    public String fileService() throws IOException {
        File wordFile = new DefaultResourceLoader()
            .getResource("file:D:/JDH Post-IPO_Share_Award_Scheme 202307-1.docx").getFile();

        File signatureFile = new DefaultResourceLoader()
            .getResource("file:D:/abcd.png").getFile();

        val wordSampleBytes = Files.readAllBytes(wordFile.toPath());
        val signatureSample = Files.readAllBytes(signatureFile.toPath());

        val word64 = Base64.getEncoder().encodeToString(wordSampleBytes);
        val signature64 = Base64.getEncoder().encodeToString(signatureSample);

        Map<String, String> replaceDate = Map.of("{<|<EnglishName>|>}", "matt.xia.from" +
            ".java");
        Map<String, Object> jsonData = Map.of("baseName", "baseName1",
            "allNames", List.of(
                Map.of("n", 1),
                Map.of("n", 2),
                Map.of("n", 3),
                Map.of("n", 4)
            ));
        val signatureMap = Map.of("signature", signature64);
        val fileName = "word2pdf_java.pdf";
        return "fileService return ";
    }
}
