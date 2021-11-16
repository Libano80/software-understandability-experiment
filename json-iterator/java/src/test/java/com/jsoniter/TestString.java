package com.jsoniter;

import com.jsoniter.spi.JsonException;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class TestString extends TestCase {

    static {
//        JsonIterator.enableStreamingSupport();
    }

    public void test_ascii_string() throws IOException {
        JsonIterator iter = JsonIterator.parse("'hello''world'".replace('\'', '"'));
        assertEquals("hello", iter.readString());
        assertEquals("world", iter.readString());
        iter = JsonIterator.parse("'hello''world'".replace('\'', '"'));
        assertEquals("hello", iter.readStringAsSlice().toString());
        assertEquals("world", iter.readStringAsSlice().toString());
    }

    public void test_ascii_string_with_escape() throws IOException {
        JsonIterator iter = JsonIterator.parse("'he\\tllo'".replace('\'', '"'));
        assertEquals("he\tllo", iter.readString());
    }

    public void test_utf8_string() throws IOException {
        JsonIterator iter = JsonIterator.parse("'中文'".replace('\'', '"'));
        assertEquals("中文", iter.readString());
    }

    public void test_incomplete_escape() throws IOException {
        JsonIterator iter = JsonIterator.parse("\"\\");
        try {
            iter.readString();
            fail();
        } catch (JsonException e) {
        }
    }

    public void test_surrogate() throws IOException {
        JsonIterator iter = JsonIterator.parse("\"\ud83d\udc4a\"");
        assertEquals("\ud83d\udc4a", iter.readString());
    }

    public void test_larger_than_buffer() throws IOException {
        JsonIterator iter = JsonIterator.parse("'0123456789012345678901234567890123'".replace('\'', '"'));
        assertEquals("0123456789012345678901234567890123", iter.readString());
    }

    @org.junit.experimental.categories.Category(StreamingCategory.class)
    public void test_string_across_buffer() throws IOException {
        JsonIterator iter = JsonIterator.parse(new ByteArrayInputStream("'hello''world'".replace('\'', '"').getBytes()), 2);
        assertEquals("hello", iter.readString());
        assertEquals("world", iter.readString());
        iter = JsonIterator.parse(new ByteArrayInputStream("'hello''world'".replace('\'', '"').getBytes()), 2);
        assertEquals("hello", iter.readStringAsSlice().toString());
        assertEquals("world", iter.readStringAsSlice().toString());
    }

    @org.junit.experimental.categories.Category(StreamingCategory.class)
    public void test_utf8() throws IOException {
        byte[] bytes = {'"', (byte) 0xe4, (byte) 0xb8, (byte) 0xad, (byte) 0xe6, (byte) 0x96, (byte) 0x87, '"'};
        JsonIterator iter = JsonIterator.parse(new ByteArrayInputStream(bytes), 2);
        assertEquals("中文", iter.readString());
    }

    @org.junit.experimental.categories.Category(StreamingCategory.class)
    public void test_normal_escape() throws IOException {
        byte[] bytes = {'"', (byte) '\\', (byte) 't', '"'};
        JsonIterator iter = JsonIterator.parse(new ByteArrayInputStream(bytes), 2);
        assertEquals("\t", iter.readString());
    }

    @org.junit.experimental.categories.Category(StreamingCategory.class)
    public void test_unicode_escape() throws IOException {
        byte[] bytes = {'"', (byte) '\\', (byte) 'u', (byte) '4', (byte) 'e', (byte) '2', (byte) 'd', '"'};
        JsonIterator iter = JsonIterator.parse(new ByteArrayInputStream(bytes), 2);
        assertEquals("中", iter.readString());
    }

    public void test_null_string() throws IOException {
        JsonIterator iter = JsonIterator.parse("null".replace('\'', '"'));
        assertEquals(null, iter.readString());
    }

    public void test_incomplete_string() throws IOException {
        try {
            JsonIterator.deserialize("\"abc", String.class);
            fail();
        } catch (JsonException e) {
        }
    }

    public void test_invalid_string() throws IOException {
        for (String str : new String[]{
                "\"\\x0008\"",
                "\"\\u000Z\"",
                "\"\\u000\"",
                "\"\\u00\"",
                "\"\\u0\"",
                "\"\\\"",
                "\"\\udd1e\"",
                "\"\\ud834\"",
                "\"\\ud834\\x\"",
                "\"\\ud834\\ud834\"",
        }) {
            try {JsonIterator.deserialize(str, String.class);
            } catch (JsonException e) {
            } catch (IndexOutOfBoundsException e) {
            }
        }
    }

    public void test_long_string() throws IOException {
        JsonIterator iter = JsonIterator.parse("\"[\\\"LL\\\",\\\"MM\\\\\\/LW\\\",\\\"JY\\\",\\\"S\\\",\\\"C\\\",\\\"IN\\\",\\\"ME \\\\\\/ LE\\\"]\"");
        assertEquals("[\"LL\",\"MM\\/LW\",\"JY\",\"S\",\"C\",\"IN\",\"ME \\/ LE\"]", iter.readString());
    }

    @Category(StreamingCategory.class)
    public void test_long_string_in_streaming() throws IOException {
        JsonIterator iter = JsonIterator.parse(new ByteArrayInputStream("\"[\\\"LL\\\",\\\"MM\\\\\\/LW\\\",\\\"JY\\\",\\\"S\\\",\\\"C\\\",\\\"IN\\\",\\\"ME \\\\\\/ LE\\\"]\"".getBytes()), 2);
        assertEquals("[\"LL\",\"MM\\/LW\",\"JY\",\"S\",\"C\",\"IN\",\"ME \\/ LE\"]", iter.readString());
    }

    public void test_findStringEnd_IterImplSkip() throws IOException {
        JsonIterator iter = JsonIterator.parse("\"String\"");
        CodegenAccess.nextToken(iter); // opening " skipped
        assertEquals(8, IterImplSkip.findStringEnd(iter));
    }

    public void test_findStringEnd_IterImplSkip2() throws IOException {
        JsonIterator iter = JsonIterator.parse("\"\\\"field\\\":\\\"value\\\"\"");
        CodegenAccess.nextToken(iter); // opening " skipped
        assertEquals(21, IterImplSkip.findStringEnd(iter));
    }

    public void test_findStringEnd_IterImplSkip3() throws IOException {
        JsonIterator iter = JsonIterator.parse("\"\\\"field1\\\":\\\"value1\\\"\",\"\\\"field2\\\":\\\"value2\\\"\"");
        CodegenAccess.nextToken(iter); // opening " skipped
        assertEquals(23, IterImplSkip.findStringEnd(iter));
        CodegenAccess.skipFixedBytes(iter, 22); // closing " skipped
        CodegenAccess.nextToken(iter); // , skipped
        CodegenAccess.nextToken(iter); // opening " skipped
        assertEquals(47, IterImplSkip.findStringEnd(iter));
    }

    public void test_findStringEnd_IterImplSkip4() throws IOException {
        JsonIterator iter = JsonIterator.parse("\\\"String\\\"");
        CodegenAccess.nextToken(iter); // \ skipped
        CodegenAccess.nextToken(iter); // opening " skipped
        assertEquals(-1, IterImplSkip.findStringEnd(iter));
    }

    public void test_findStringEnd_IterImplSkip5() throws IOException {
        JsonIterator iter = JsonIterator.parse("\\\\\"String\\\\\"");
        CodegenAccess.nextToken(iter); // \ skipped
        CodegenAccess.nextToken(iter); // \ skipped
        CodegenAccess.nextToken(iter); // opening " skipped
        assertEquals(12, IterImplSkip.findStringEnd(iter));
    }

    public void test_parse_IterImplString() throws IOException {
        JsonIterator iter = JsonIterator.parse("\"String\"");
        CodegenAccess.nextToken(iter); // " skipped
        assertEquals(6, IterImplString.parse(iter));
    }

    public void test_parse_IterImplString2() throws IOException {
        JsonIterator iter = JsonIterator.parse("\"field\":\"value\"");
        CodegenAccess.nextToken(iter); // " skipped
        assertEquals(5, IterImplString.parse(iter));
        CodegenAccess.nextToken(iter); // : skipped
        CodegenAccess.nextToken(iter); // " skipped
        assertEquals(5, IterImplString.parse(iter));
    }

    public void test_parse_IterImplString3() throws IOException {
        JsonIterator iter = JsonIterator.parse("\"12345678901234567890\"");
        CodegenAccess.nextToken(iter); // " skipped
        assertEquals(20, IterImplString.parse(iter));
    }

    public void test_parse_IterImplString4() throws IOException {
        JsonIterator iter = JsonIterator.parse("\"12345678901234567890\",\"12345678901234567890\"");
        CodegenAccess.nextToken(iter); // " skipped
        assertEquals(20, IterImplString.parse(iter));
        CodegenAccess.nextToken(iter); // , skipped
        CodegenAccess.nextToken(iter); // " skipped
        assertEquals(20, IterImplString.parse(iter));
    }

    public void test_parse_IterImplString5() throws IOException {
        JsonIterator iter = JsonIterator.parse("\"1234567890123456789012345678901234567890\"");
        CodegenAccess.nextToken(iter); // " skipped
        assertEquals(40, IterImplString.parse(iter));
    }
}
