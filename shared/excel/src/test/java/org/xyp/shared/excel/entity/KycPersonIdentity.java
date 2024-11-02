package org.xyp.shared.excel.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KycPersonIdentity {
    Long taskId;
    IdProofDocEnum idProofDocType;
    String identityOtherProofDocType;

    String idDocIssueCountry;
    String idDocNo;
    LocalDate idDocIssueDate;
    LocalDate expiryDateOfIdDoc;

    String chineseFamilyNameRO;
    String chineseGivenNameRO;
    String chineseFullNameRO;
    String englishMidNameRO;
    String englishGivenNameRO;
    String englishFamilyNameRO;
    String genderRO;
    String emailRO;

    String nationality;
    LocalDate dateOfBirth;
    String placeOfBirth;

    List<KycPersonTaxCode> taxCodes;
    LocalDate employmentDate;
    LocalDate terminationDate;

}
