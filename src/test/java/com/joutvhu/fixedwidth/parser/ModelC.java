package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.annotation.FixedField;
import com.joutvhu.fixedwidth.parser.annotation.FixedParam;

import java.util.List;
import java.util.Map;

public class ModelC {
    @FixedField(label = "LIST-MAP", length = 30)
    private List<@FixedParam(length = 10) Map<@FixedParam(length = 2) String, @FixedParam(length = 2) String>> listMap;
}
