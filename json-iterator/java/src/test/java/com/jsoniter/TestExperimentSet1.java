package com.jsoniter;

import com.jsoniter.extra.GsonCompatibilityMode;
import com.jsoniter.spi.Decoder;
import com.jsoniter.spi.Slice;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class TestExperimentSet1 extends TestCase {

    public void test_createDecoder_GsonCompatibilityMode() throws IOException {
        GsonCompatibilityMode gsonCompatibilityMode = new GsonCompatibilityMode.Builder()
                .setDateFormat("EEE, MMM d, yyyy hh:mm:ss a z").build();
        Decoder decoder = gsonCompatibilityMode.createDecoder("", Date.class);
        JsonIterator iter = JsonIterator.parse("\"Thu, Jan 1, 1970 12:00:00 AM UTC\"");
        assertEquals(0, ((Date) decoder.decode(iter)).getTime());
    }

    public void test_createDecoder_GsonCompatibilityMode2() throws IOException {
        GsonCompatibilityMode gsonCompatibilityMode = new GsonCompatibilityMode.Builder().build();
        Decoder decoder = gsonCompatibilityMode.createDecoder("", String.class);
        JsonIterator iter = JsonIterator.parse("\"field\"");
        assertEquals("field", decoder.decode(iter));
        iter = JsonIterator.parse("100");
        assertEquals("100", decoder.decode(iter));
        iter = JsonIterator.parse("false");
        assertEquals("false", decoder.decode(iter));
        iter = JsonIterator.parse("null");
        assertNull(decoder.decode(iter));
    }

    public void test_createDecoder_GsonCompatibilityMode3() throws IOException {
        GsonCompatibilityMode gsonCompatibilityMode = new GsonCompatibilityMode.Builder().build();
        Decoder decoder = gsonCompatibilityMode.createDecoder("", boolean.class);
        JsonIterator iter = JsonIterator.parse("false");
        assertFalse((Boolean) decoder.decode(iter));
        iter = JsonIterator.parse("true");
        assertTrue((Boolean) decoder.decode(iter));
    }

    public void test_createDecoder_GsonCompatibilityMode4() throws IOException {
        GsonCompatibilityMode gsonCompatibilityMode = new GsonCompatibilityMode.Builder().build();
        Decoder decoder = gsonCompatibilityMode.createDecoder("", long.class);
        JsonIterator iter = JsonIterator.parse("100");
        assertEquals(100L, decoder.decode(iter));
        iter = JsonIterator.parse("null");
        assertEquals(0L, decoder.decode(iter));
    }

    public void test_createDecoder_GsonCompatibilityMode5() throws IOException {
        GsonCompatibilityMode gsonCompatibilityMode = new GsonCompatibilityMode.Builder().build();
        Decoder decoder = gsonCompatibilityMode.createDecoder("", int.class);
        JsonIterator iter = JsonIterator.parse("100");
        assertEquals(100, decoder.decode(iter));
        iter = JsonIterator.parse("null");
        assertEquals(0, decoder.decode(iter));
    }

    public void test_createDecoder_GsonCompatibilityMode6() throws IOException {
        GsonCompatibilityMode gsonCompatibilityMode = new GsonCompatibilityMode.Builder().build();
        Decoder decoder = gsonCompatibilityMode.createDecoder("", double.class);
        JsonIterator iter = JsonIterator.parse("100");
        assertEquals(100d, decoder.decode(iter));
        iter = JsonIterator.parse("null");
        assertEquals(0d, decoder.decode(iter));
    }

    public void test_createDecoder_GsonCompatibilityMode7() throws IOException {
        GsonCompatibilityMode gsonCompatibilityMode = new GsonCompatibilityMode.Builder().build();
        Decoder decoder = gsonCompatibilityMode.createDecoder("", float.class);
        JsonIterator iter = JsonIterator.parse("100");
        assertEquals(100f, decoder.decode(iter));
        iter = JsonIterator.parse("null");
        assertEquals(0f, decoder.decode(iter));
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

    public void testReadSlice() throws IOException {
        JsonIterator iter = JsonIterator.parse("{\"field1\":\"field1\"}");
        CodegenAccess.nextToken(iter);
        Slice slice = IterImplForStreaming.readSlice(iter);
        assertEquals("field1", new String(slice.data(), slice.head(), slice.tail() - slice.head()));
    }

    public void testReadSlice2() throws IOException {
        JsonIterator iter = JsonIterator.parse("{\"field\":\"value\"}");
        CodegenAccess.nextToken(iter); // { skipped
        Slice field = IterImplForStreaming.readSlice(iter);
        assertEquals("field", new String(field.data(), field.head(), field.tail() - field.head()));
        CodegenAccess.nextToken(iter); // : skipped
        Slice value = IterImplForStreaming.readSlice(iter);
        assertEquals("value", new String(value.data(), value.head(), value.tail() - value.head()));
    }

    // In order to pass, this method should be run by itself
    @Category(StreamingCategory.class)
    public void testReadSlice3() throws IOException {
        InputStream in = new ByteArrayInputStream("{\"field\":\"value\"}".getBytes());
        JsonIterator iter = JsonIterator.parse(in, 2);
        CodegenAccess.nextToken(iter); // { skipped
        Slice field = IterImplForStreaming.readSlice(iter);
        assertEquals("field", new String(field.data(), field.head(), field.tail() - field.head()));
        CodegenAccess.nextToken(iter); // : skipped
        Slice value = IterImplForStreaming.readSlice(iter);
        assertEquals("value", new String(value.data(), value.head(), value.tail() - value.head()));
    }

    // In order to pass, this method should be run by itself
    @Category(StreamingCategory.class)
    public void testReadSlice4() throws IOException {
        InputStream in = new ByteArrayInputStream("{\"field\":\"value\"}".getBytes());
        JsonIterator iter = JsonIterator.parse(in, 5);
        CodegenAccess.nextToken(iter); // { skipped
        Slice field = IterImplForStreaming.readSlice(iter);
        assertEquals("field", new String(field.data(), field.head(), field.tail() - field.head()));
        CodegenAccess.nextToken(iter); // : skipped
        Slice value = IterImplForStreaming.readSlice(iter);
        assertEquals("value", new String(value.data(), value.head(), value.tail() - value.head()));
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

}
