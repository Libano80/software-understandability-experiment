package com.jsoniter;

import com.jsoniter.spi.Slice;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class IterImplForStreamingTest extends TestCase {

    public void testReadMaxDouble() throws Exception {
        String maxDouble = "1.7976931348623157e+308";
        JsonIterator iter = JsonIterator.parse("1.7976931348623157e+308");
        IterImplForStreaming.numberChars numberChars = IterImplForStreaming.readNumber(iter);
        String number = new String(numberChars.chars, 0, numberChars.charsLength);
        assertEquals(maxDouble, number);
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

}