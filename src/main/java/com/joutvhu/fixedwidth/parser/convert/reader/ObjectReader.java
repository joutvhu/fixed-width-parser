package com.joutvhu.fixedwidth.parser.convert.reader;

import com.joutvhu.fixedwidth.parser.convert.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.ReadStrategy;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.FixedHelper;
import com.joutvhu.fixedwidth.parser.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author Giao Ho
 * @since 1.0.0
 */
public class ObjectReader extends FixedWidthReader<Object> {
    public ObjectReader(FixedTypeInfo info, ReadStrategy strategy) {
        super(info, strategy);
        if (info.getFixedObject() == null) this.reject();
    }

    @Override
    public Object read(StringAssembler assembler) {
        Object result = FixedHelper.newInstanceOf(info.getType());
        List<FixedTypeInfo> children = info.getElementTypeInfo();
        for (FixedTypeInfo fieldInfo : children) {
            Field field = fieldInfo.getField();
            if (field != null) {
                StringAssembler childAssembler = assembler.child(fieldInfo);
                Object v = read(fieldInfo, childAssembler);

                ReflectionUtil.makeAccessible(field);
                ReflectionUtil.setField(field, result, v);
            }
        }
        return result;
    }
}
