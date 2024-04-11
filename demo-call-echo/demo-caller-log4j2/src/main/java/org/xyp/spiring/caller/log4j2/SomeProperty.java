package org.xyp.spiring.caller.log4j2;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Data;

@Data
@ConfigurationProperties(prefix = "some.prop")
public class SomeProperty {
	String name;
	String desc;
}
