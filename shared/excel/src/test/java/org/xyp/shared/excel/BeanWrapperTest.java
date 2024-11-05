package org.xyp.shared.excel;

import lombok.val;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.xyp.shared.excel.entity.KycPersonAddress;
import org.xyp.shared.excel.entity.KycTask;
import org.xyp.shared.utils.BeanUtils;

import java.lang.reflect.InvocationTargetException;

class BeanWrapperTest {
    @Test
    void testBeanWrapper() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        KycTask task = KycTask.builder()
            .employeeId("employee")
            .addressInfo(KycPersonAddress.builder()
                .postCode("postcode12345")
                .build())
            .build();

        val eid = BeanUtils.propertyValue(task, "employeeId");
        Assertions.assertThat(eid).isEqualTo("employee");
        val postCode = BeanUtils.propertyValue(task, "addressInfo.postCode");
        Assertions.assertThat(postCode).isEqualTo("postcode12345");

        Assertions.assertThat(BeanUtils.propertyValue(task, "idInfo.idDocNo")).isNull();
    }
}
