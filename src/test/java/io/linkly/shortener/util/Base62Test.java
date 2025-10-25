package io.linkly.shortener.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Base62Test {

    @Test
    void encodeDecodeRoundTrip() {
        long[] values = {0L, 1L, 61L, 62L, 12345L, Integer.MAX_VALUE, 9876543210L};
        for (long v : values) {
            String enc = Base62.encode(v);
            long dec = Base62.decode(enc);
            assertEquals(v, dec);
        }
    }

    @Test
    void encodeRejectsNegative() {
        assertThrows(IllegalArgumentException.class, () -> Base62.encode(-1));
    }

    @Test
    void decodeRejectsInvalidChars() {
        assertThrows(IllegalArgumentException.class, () -> Base62.decode("abc$"));
    }
}


