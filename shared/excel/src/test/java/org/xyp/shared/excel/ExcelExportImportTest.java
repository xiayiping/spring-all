package org.xyp.shared.excel;


import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.*;
import org.springframework.test.context.ActiveProfiles;
import org.xyp.shared.excel.entity.KycBatch;
import org.xyp.shared.excel.entity.KycFlowStatusEnum;
import org.xyp.shared.excel.entity.KycPersonIdentity;
import org.xyp.shared.excel.entity.KycTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@ActiveProfiles("test")
class ExcelExportImportTest {
    final ExportConfigurationContainer exportConfigurationContainer;
    final ImportConfigurationContainer importConfigurationContainer;
    final ExcelImporter importer;
    final ExcelExporter exporter;

    public ExcelExportImportTest() {
        val properties = CorejExcelProperties.builder()
            .exportConfigRoot("classpath:/excel/export")
            .importConfigRoot("classpath:/excel/import")
            .build();
        exportConfigurationContainer = new ExportConfigurationContainer(properties);
        importConfigurationContainer = new ImportConfigurationContainer(properties);
        importer = ExcelImporter.newInstance(importConfigurationContainer);
        exporter = ExcelExporter.newInstance(exportConfigurationContainer);
    }

    static final String tmp = "tmp";

    List<KycTask> tasks = List.of(
        KycTask.builder().id(11L).employeeId("taskEmployee").companyId(123)
            .idInfo(KycPersonIdentity.builder()
                .chineseFamilyNameRO("zhongwen")
                .chineseGivenNameRO("mingzi1")
                .dateOfBirth(LocalDate.of(1999, 1, 1))
                .idDocIssueDate(LocalDate.of(2000, 2, 3))
                .expiryDateOfIdDoc(LocalDate.of(2100, 2, 3))
                .build())
            .kycBatch(KycBatch.builder().batchName("batch1").build())
            .status(KycFlowStatusEnum.PARTICIPANT_INPUT)
            .lastUpdateTime(LocalDateTime.now()).build()
        , KycTask.builder().id(12L).employeeId("taskEmployee2").companyId(456)
            .idInfo(KycPersonIdentity.builder()
                .chineseFamilyNameRO("zhongwen")
                .chineseGivenNameRO("mingzi2")
                .dateOfBirth(LocalDate.of(2013, 1, 1))
                .idDocIssueDate(LocalDate.of(2001, 2, 3))
                .expiryDateOfIdDoc(LocalDate.of(2101, 2, 3))
                .build())
            .kycBatch(KycBatch.builder().batchName("batch2").build())
            .status(KycFlowStatusEnum.COMPANY_CHECKING)
            .build()
        , KycTask.builder().id(13L).employeeId("taskEmployee3").companyId(456)
            .lastUpdateTime(LocalDateTime.now())
            .idInfo(KycPersonIdentity.builder()
                .chineseFamilyNameRO("zhongwen")
                .chineseGivenNameRO("mingzi3")
                .dateOfBirth(LocalDate.of(2999, 1, 1))
                .idDocIssueDate(LocalDate.of(2002, 2, 3))
                .expiryDateOfIdDoc(LocalDate.of(2102, 2, 3))
                .build())
            .kycBatch(KycBatch.builder().batchName("batch3").build())
            .status(KycFlowStatusEnum.TCT_CHECK_VERIFY_FILES)
            .build()
    );

    byte[] excelBytes = null;

    @Test
    @Order(0)
    void testExportConfigExists() {
        val configNames = exportConfigurationContainer.getConfigNames();
        System.out.println(configNames);
        Assertions.assertThat(configNames).isNotNull().isNotEmpty();
    }

    @Test
    @Order(10)
    void testExport() throws Exception {
        Assertions.assertThatNoException().isThrownBy(() -> {
            val book = exporter.exportObjectList("kycTask", tasks);
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            book.write(bout);
            val bytes = bout.toByteArray();
            book.close();
            bout.close();

            excelBytes = bytes;
            val tmpFolder = new File(tmp);
            if (!tmpFolder.exists()) {
                tmpFolder.mkdirs();
            }

            Files.write(new File(tmpFolder, "kycTask.xlsx").toPath()
                , bytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        });
    }

    @Test
    @Order(20)
    void testImportKycTask() {
        val list = importer.importFromSourceToMap(excelBytes, "kycTaskMap");
        System.out.println(list);
        Assertions.assertThat(list).hasSize(3);
    }
}
