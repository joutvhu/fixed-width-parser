package com.joutvhu.fixedwidth.parser;

import com.joutvhu.fixedwidth.parser.domain.Event;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

public class ReaderTests {
    @Test
    public void read() {
        Event event = FixedParser
                .parser()
                .parse("40023nkjnssednfwmkerkjwernkwtyd4emw11nerjkwermfwlmkneriwjfnerijwne", Event.class);
        Assert.assertNotNull(event);
        String line = FixedParser
                .parser()
                .write(event);
//        Assert.assertNotNull(line);
    }
}
