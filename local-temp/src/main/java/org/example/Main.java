package org.example;

import com.fasterxml.jackson.core.StreamReadConstraints;
import com.tcg.paradise.common.service.document.api.DocumentService;
import com.tcg.paradise.common.service.document.api.pojo.*;
import com.tcg.paradise.common.service.document.shared.SharedApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.util.ResourceUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.fasterxml.jackson.core.StreamReadConstraints.DEFAULT_MAX_NUM_LEN;
import static com.fasterxml.jackson.core.StreamReadConstraints.DEFAULT_MAX_STRING_LEN;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        StreamReadConstraints.overrideDefaultStreamReadConstraints(
                StreamReadConstraints.builder()
                        .maxNestingDepth(StreamReadConstraints.DEFAULT_MAX_DEPTH)
                        .maxNumberLength(StreamReadConstraints.DEFAULT_MAX_NUM_LEN)
                        .maxStringLength(StreamReadConstraints.DEFAULT_MAX_STRING_LEN << 2)
                        .build()
        );

        SpringApplication.run(Main.class);

    }

    @Autowired
    DocumentService documentService;

    @Bean
    public ApplicationRunner runner() {

        /*
        StreamReadConstraints.DEFAULT_MAX_DEPTH,
        StreamReadConstraints.DEFAULT_MAX_NUM_LEN,
        StreamReadConstraints.DEFAULT_MAX_STRING_LEN
         */
        return args -> {
            byte[] wordBytes = Files.readAllBytes(ResourceUtils.getFile("file:D:\\develop\\paradise\\tcg-paradise\\tcg-paradise-app\\src\\main\\resources\\template\\sertus\\bvi\\incorp\\template.doc").toPath());

            byte[] picBytes = Files.readAllBytes(ResourceUtils.getFile("file:D:\\test2.png").toPath());

            Map<String, Object> datMap = Map.of(
                    "client", Map.of("name", "temp client name"),
                    "sig", Map.of(
                            "dt0", "{sign.150x30.dat0}",
                            "bvi0", "{sign.150x30.bvn0}",
                            "rep0", "{sign.150x30.rep0}",
                            "per0", "{sign.150x30.per0}",
                            "tit0", "{sign.150x22.tit0}",
                            "dt1", "{sign.150x30.dat1}",
                            "bvi1", "{sign.150x30.bvn1}",
                            "rep1", "{sign.150x30.rep1}",
                            "dir1", "{sign.150x22.dir1}"
                    )
            );
            /*
        "dt0": "{sign.150x30.dat1}",
        "bvi0": "{sign.150x30.bvn1}",
        "rep0": "{sign.150x30.rep1}",
        "tit0": "{sign.150x22.dir1}",
        "dt1": "{sign.150x30.dat1}",
        "bvi1": "{sign.150x30.bvn1}",
        "rep1": "{sign.150x30.rep1}",
        "dir1": "{sign.150x22.dir1}"
             */
            var genReq = new PdfGenerateRequest(RequestMetaInfo.builder().build(),
                    Map.of(), datMap, Map.of(),
                    SharedApi.encodeBase64(wordBytes));
            FileGenerateResponse genResp = documentService.generatePdf(genReq);
            Path outputGenPath = Path.of("d:/paradise.pdf");
            Files.write(outputGenPath,
                    SharedApi.decodeBase64(genResp.content()),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println(outputGenPath);

            byte[] pdf = Files.readAllBytes(outputGenPath);
            var imgReq = new ConvertPdfToImageRequest(RequestMetaInfo.builder().build(),
                    SharedApi.encodeBase64(pdf), 3F);
            ConvertPdfToImageResponse imgResp = documentService.convertPdfToImage(imgReq);
            imgResp.signaturePlaceHolderInfos().forEach(System.out::println);
            Assert.isTrue(imgResp.signaturePlaceHolderInfos().size() > 0, "size not empty");


            var signReq = new SignatureOnPositionRequest(
                    RequestMetaInfo.builder().build(),
                    SharedApi.encodeBase64(pdf),
                    imgResp.signaturePlaceHolderInfos().stream()
                            .map(first -> new ImagePositionInfo(first.getPageIdx(),
                                    first.x(), first.y(),
                                    first.getPageWidth(), first.getPageHeight(),
                                    SharedApi.encodeBase64(picBytes), false))
                            .toList());
            var signResp = documentService.signaturePdf(signReq);

            Path outputSignedPath = Path.of("d:/paradise-signed.pdf");
            Files.write(outputSignedPath,
                    SharedApi.decodeBase64(signResp.content()),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println(outputSignedPath);


            byte[] pdfSigned = Files.readAllBytes(outputSignedPath);
            var imgReqSigned = new ConvertPdfToImageRequest(RequestMetaInfo.builder().build(),
                    SharedApi.encodeBase64(pdfSigned), 3F);
            ConvertPdfToImageResponse imgRespSigned = documentService.convertPdfToImage(imgReqSigned);
            imgRespSigned.signaturePlaceHolderInfos().forEach(System.out::println);
            Assert.isTrue(imgRespSigned.signaturePlaceHolderInfos().size() > 0, "size not empty");
            Assert.isTrue(imgRespSigned.signaturePlaceHolderInfos().stream().filter(SignaturePositionInfo::isWaitingForSignature).count() == 0, "all signed");


            var cancelReq = new SignatureOnPositionRequest(
                    RequestMetaInfo.builder().build(),
                    signResp.content(),
                    imgResp.signaturePlaceHolderInfos().stream()
                            .map(first -> new ImagePositionInfo(first.getPageIdx(),
                                    first.x(), first.y(),
                                    first.getPageWidth(), first.getPageHeight(),
                                    SharedApi.encodeBase64(picBytes), true))
                            .findAny().stream().toList());
            var cancelResp = documentService.signaturePdf(cancelReq);
            Path outputCancelPath = Path.of("d:/paradise-cancel.pdf");
            Files.write(outputCancelPath,
                    SharedApi.decodeBase64(cancelResp.content()),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            System.out.println(outputCancelPath);

            var imgReqCanceled = new ConvertPdfToImageRequest(RequestMetaInfo.builder().build(),
                    cancelResp.content(), 2F);
            ConvertPdfToImageResponse imgRespCanceled = documentService.convertPdfToImage(imgReqCanceled);
            imgRespCanceled.signaturePlaceHolderInfos().forEach(System.out::println);
            Assert.isTrue(imgRespCanceled.signaturePlaceHolderInfos().size() > 0, "size not empty");
            Assert.isTrue(imgRespCanceled.signaturePlaceHolderInfos().stream().filter(SignaturePositionInfo::isWaitingForSignature).count() == 1, "all signed");
        };
    }
}