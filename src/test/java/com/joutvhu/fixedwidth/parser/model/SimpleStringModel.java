package com.joutvhu.fixedwidth.parser.model;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Minimal model: một field string duy nhất.
 * Dùng để test các tính năng cơ bản mà không có noise từ các field khác.
 */
@FixedObject
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimpleStringModel {
    @FixedField(length = 10)
    private String value;
}
