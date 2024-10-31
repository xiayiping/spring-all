package org.xyp.shared.db.datasource;

import lombok.Getter;

@Getter
enum HibernateToDdlEnum {

    Validate("validate"),Update("update"),

    Create("create"),CreateDrop("create-drop");

    private final String value;

    HibernateToDdlEnum(String value) {
        this.value = value;
    }

}