package org.xyp.demo.echo;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public EchoController() {
        log.info("EchoController created");
    }

    private Random random = new Random();

    @GetMapping("/")
    public String base() {
        log.info("base from echo");
        return "base echo " + random.nextInt(100);
    }

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
