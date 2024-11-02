package org.xyp.shared.excel;

import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.beans.NullValueInNestedPathException;
import org.xyp.shared.excel.entity.KycPersonAddress;
import org.xyp.shared.excel.entity.KycTask;

class BeanWrapperTest {
    @Test
    void testBeanWrapper() {
        KycTask task = KycTask.builder()
            .employeeId("employee")
            .addressInfo(KycPersonAddress.builder()
                .postCode("postcode12345")
                .build())
            .build();
        BeanWrapper beanWrapper = new BeanWrapperImpl(task);

        val eid = beanWrapper.getPropertyValue("employeeId");
        Assertions.assertThat(eid).isEqualTo("employee");
        val postCode = beanWrapper.getPropertyValue("addressInfo.postCode");
        Assertions.assertThat(postCode).isEqualTo("postcode12345");

        Assertions.assertThatThrownBy(() -> beanWrapper.getPropertyValue("idInfo.idDocNo"))
            .isInstanceOf(NullValueInNestedPathException.class);
    }
}
