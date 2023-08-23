package org.xyp.demo.call;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.NotNull;
import lombok.val;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Controller
public class InterviewController {

    @GetMapping("infile")
    public ResponseEntity<byte[]> file() throws IOException {
        val filename = "Electricity.pdf";
        val bytes = Files.readAllBytes(Path.of("d:/", filename));

        return ResponseEntity.ok()
                .contentLength(bytes.length)
                .header(HttpHeaders.CONTENT_DISPOSITION, // open
                        ContentDisposition.attachment()
                                .filename(filename)
                                .build().toString())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
    }

    @GetMapping("interview")
    public String interview() {
        return "interview.ftl";
    }

    @Operation(summary = "Display a signature page for demo")
    @PostMapping("/interview/submit")
    public String interviewSubmit(@RequestPart("file") @NotNull MultipartFile file)
            throws IOException {
        Files.write(Path.of("d:", "interview.zip"), file.getBytes());
        return "interview.ftl";
    }
}
