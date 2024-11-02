package org.xyp.shared.excel.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class KycPersonAddress {
    LocalDate issueDateOfAddressProof;
    AddressProofEnum addressProofType;
    @JsonIgnore // avoid front page duplicate field name
    String otherProofDocType;
    LocalDate expiryDateOfAddressProof;
    String phoneCountry;
    String phoneArea;
    String phoneNumber;
    String postCode;

    String countryChinese;
    String provinceChinese;
    String cityChinese;
    String areaChinese;
    String streetChinese;
    String addressTailChinese;
    String countryEnglish;
    String provinceEnglish;
    String cityEnglish;
    String areaEnglish;
    String streetEnglish;
    String addressTailEnglish;

}
