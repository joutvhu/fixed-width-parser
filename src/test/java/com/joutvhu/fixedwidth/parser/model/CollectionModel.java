package com.joutvhu.fixedwidth.parser.model;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedObject;
import com.joutvhu.fixedwidth.parser.annotation.FixedParam;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Collection model: used to test List and Map fields.
 */
@FixedObject
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CollectionModel {
    @FixedField(length = 9)
    private List<@FixedParam(length = 3) String> items;

    @FixedField(start = 9, length = 12)
    private Map<@FixedParam(length = 2) String, @FixedParam(length = 4) String> mapping;
}
