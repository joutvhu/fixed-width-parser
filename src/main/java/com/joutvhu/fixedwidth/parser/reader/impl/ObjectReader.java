package com.joutvhu.fixedwidth.parser.reader.impl;

import com.joutvhu.fixedwidth.parser.exception.FixedException;
import com.joutvhu.fixedwidth.parser.reader.FixedWidthReader;
import com.joutvhu.fixedwidth.parser.support.FixedObjectHelper;
import com.joutvhu.fixedwidth.parser.support.FixedParseStrategy;
import com.joutvhu.fixedwidth.parser.support.FixedTypeInfo;
import com.joutvhu.fixedwidth.parser.support.StringAssembler;
import com.joutvhu.fixedwidth.parser.util.ReflectionUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class ObjectReader extends FixedWidthReader<Object> {
    public ObjectReader(FixedTypeInfo info, FixedParseStrategy strategy) {
        super(info, strategy);
        if (info.getFixedObject() == null) this.skip();
    }

    @Override
    public Object read(StringAssembler assembler) {
        try {
            Object result = FixedObjectHelper.newInstanceOf(info.getType());
            List<FixedTypeInfo> children = info.getChildInfo();
            for (FixedTypeInfo fieldInfo : children) {
                Field field = fieldInfo.getField();
                if (field != null) {
                    StringAssembler childAssembler = assembler.child(fieldInfo);
                    Object v = strategy.read(fieldInfo, childAssembler);

                    ReflectionUtil.makeAccessible(field);
                    ReflectionUtil.setField(field, result, v);
                }
            }
            return result;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            throw new FixedException(e);
        }
    }
}
