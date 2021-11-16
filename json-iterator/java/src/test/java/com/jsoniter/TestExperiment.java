package com.jsoniter;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingStrategy;
import com.jsoniter.annotation.JsonProperty;
import com.jsoniter.any.Any;
import com.jsoniter.any.TestObjectLazyAny;
import com.jsoniter.extra.GsonCompatibilityMode;
import com.jsoniter.extra.TestBase64Float;
import com.jsoniter.output.EncodingMode;
import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.*;
import junit.framework.TestCase;
import org.junit.experimental.categories.Category;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import static org.junit.Assert.assertNotEquals;

public class TestExperiment extends TestCase {

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

    //-----------------------------------------------------
    //-----------------------------------------------------
    //-----------------------------------------------------

    public void test_equalsConfigBuilder() {
        GsonCompatibilityMode.Builder builder1 = new GsonCompatibilityMode.Builder();
        GsonCompatibilityMode.Builder builder2 = new GsonCompatibilityMode.Builder();
        assertEquals(builder1, builder2);
        assertEquals(builder2, builder1);
    }

    public void test_equalsConfigBuilder2() {
        GsonCompatibilityMode.Builder builder1 = new GsonCompatibilityMode.Builder();
        GsonCompatibilityMode.Builder builder2 = builder1;
        assertEquals(builder1, builder2);
        assertEquals(builder2, builder1);
    }

    public void test_equalsConfigBuilder3() {
        GsonCompatibilityMode.Builder builder1 =
                new GsonCompatibilityMode.Builder()
                        .excludeFieldsWithoutExposeAnnotation()
                        .disableHtmlEscaping()
                        .setDateFormat(DateFormat.LONG);
        GsonCompatibilityMode.Builder builder2 =
                new GsonCompatibilityMode.Builder()
                        .excludeFieldsWithoutExposeAnnotation()
                        .disableHtmlEscaping()
                        .setDateFormat(DateFormat.LONG);
        assertEquals(builder1, builder2);
        assertEquals(builder2, builder1);
    }

    public void test_equalsConfigBuilder4() {
        FieldNamingStrategy fieldNamingStrategy = new FieldNamingStrategy() {
            @Override
            public String translateName(Field f) {
                return "_" + f.getName();
            }
        };
        GsonCompatibilityMode.Builder builder1 =
                new GsonCompatibilityMode.Builder()
                        .setFieldNamingStrategy(fieldNamingStrategy);
        GsonCompatibilityMode.Builder builder2 =
                new GsonCompatibilityMode.Builder();
        assertNotEquals(builder1, builder2);
        assertNotEquals(builder2, builder1);
    }

    public void test_equalsConfigBuilder5() {
        GsonCompatibilityMode.Builder builder1 =
                new GsonCompatibilityMode.Builder()
                        .setVersion(1.0);
        GsonCompatibilityMode.Builder builder2 =
                new GsonCompatibilityMode.Builder();
        assertNotEquals(builder1, builder2);
        assertNotEquals(builder2, builder1);
    }

    public void test_equalsConfigBuilder6() {
        ExclusionStrategy exclusionStrategy = new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return !f.getName().equals("field");
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        };
        GsonCompatibilityMode.Builder builder1 =
                new GsonCompatibilityMode.Builder()
                        .addSerializationExclusionStrategy(exclusionStrategy)
                        .addDeserializationExclusionStrategy(exclusionStrategy);
        GsonCompatibilityMode.Builder builder2 =
                new GsonCompatibilityMode.Builder();
        assertNotEquals(builder1, builder2);
        assertNotEquals(builder2, builder1);
    }

    public void test_equalsConfigBuilder7() {
        FieldNamingStrategy fieldNamingStrategy = new FieldNamingStrategy() {
            @Override
            public String translateName(Field f) {
                return "_" + f.getName();
            }
        };
        ExclusionStrategy exclusionStrategy = new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes f) {
                return !f.getName().equals("field");
            }

            @Override
            public boolean shouldSkipClass(Class<?> clazz) {
                return false;
            }
        };
        GsonCompatibilityMode.Builder builder1 =
                new GsonCompatibilityMode.Builder()
                        .setFieldNamingStrategy(fieldNamingStrategy)
                        .setVersion(1.0)
                        .addSerializationExclusionStrategy(exclusionStrategy)
                        .addDeserializationExclusionStrategy(exclusionStrategy);
        GsonCompatibilityMode.Builder builder2 =
                new GsonCompatibilityMode.Builder()
                        .setFieldNamingStrategy(fieldNamingStrategy)
                        .setVersion(1.0)
                        .addSerializationExclusionStrategy(exclusionStrategy)
                        .addDeserializationExclusionStrategy(exclusionStrategy);
        assertEquals(builder1, builder2);
        assertEquals(builder2, builder1);
    }

    //-----------------------------------------------------
    //-----------------------------------------------------
    //-----------------------------------------------------

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

    //-----------------------------------------------------
    //-----------------------------------------------------
    //-----------------------------------------------------

    private ByteArrayOutputStream baos;
    private JsonStream stream;

    public void setUp() {
        baos = new ByteArrayOutputStream();
        stream = new JsonStream(baos, 4096);
    }

    public void test_raw() throws IOException {
        stream = new JsonStream(baos, 32);
        String val = "Hello world!";
        stream.writeRaw(val, val.length());
        stream.close();
        assertEquals(val.replace('\'', '"'), baos.toString());
    }

    public void test_raw2() throws IOException {
        stream = new JsonStream(null, 32);
        String val = "Hello world!";
        stream.writeRaw(val, val.length());
        stream.close();
        assertEquals("", baos.toString());
    }

    public void test_raw3() throws IOException {
        stream = new JsonStream(baos, 32);
        String val = "12345678901234567890";
        stream.writeRaw(val, val.length());
        stream.close();
        assertEquals(val.replace('\'', '"'), baos.toString());
    }

    public void test_raw4() throws IOException {
        stream = new JsonStream(baos, 32);
        String val = "12345678901234567890123456789012";
        stream.writeRaw(val, val.length());
        stream.close();
        assertEquals(val.replace('\'', '"'), baos.toString());
    }

    public void test_raw5() throws IOException {
        stream = new JsonStream(baos, 32);
        String val = "1234567890123456789012345678901234567890";
        stream.writeRaw(val, val.length());
        stream.close();
        assertEquals(val.replace('\'', '"'), baos.toString());
    }

    //-----------------------------------------------------
    //-----------------------------------------------------
    //-----------------------------------------------------

    public void test_to_boolean_StringAny() {
        Any any = Any.wrap("hello");
        assertTrue(any.toBoolean());
    }

    public void test_to_boolean_StringAny2() {
        Any any = Any.wrap(" hello ");
        assertTrue(any.toBoolean());
    }

    public void test_to_boolean_StringAny3() {
        Any any = Any.wrap("test");
        assertTrue(any.toBoolean());
    }

    public void test_to_boolean_StringAny4() {
        Any any = Any.wrap("false");
        assertFalse(any.toBoolean());
    }

    public void test_to_boolean_StringAny5() {
        Any any = Any.wrap("False");
        assertFalse(any.toBoolean());
    }

    public void test_to_boolean_StringAny6() {
        Any any = Any.wrap("FALSe");
        assertFalse(any.toBoolean());
    }

    //-----------------------------------------------------
    //-----------------------------------------------------
    //-----------------------------------------------------

    public void test_skipFixedBytes_IterImplForStreaming() throws IOException {
        JsonIterator iter = JsonIterator.parse("{\"field\":\"hello\"}");
        IterImplForStreaming.skipFixedBytes(iter, 1);
        assertEquals(1, iter.head);
        assertEquals('"', iter.buf[iter.head]);
    }

    public void test_skipFixedBytes_IterImplForStreaming2() throws IOException {
        JsonIterator iter = JsonIterator.parse("{\"field\":\"hello\"}");
        IterImplForStreaming.skipFixedBytes(iter, 8);
        assertEquals(8, iter.head);
        assertEquals(':', iter.buf[iter.head]);
    }

    public void test_skipFixedBytes_IterImplForStreaming3() throws IOException {
        JsonIterator iter = JsonIterator.parse("{\"field\":\"hello\"}");
        IterImplForStreaming.skipFixedBytes(iter, 17);
        assertEquals(17, iter.head);
        assertEquals(17, iter.buf.length);
        assertEquals(17, iter.tail);
    }

    public void test_skipFixedBytes_IterImplForStreaming4() throws IOException {
        JsonIterator iter = JsonIterator.parse("{\"field\":\"hello\"}");
        IterImplForStreaming.skipFixedBytes(iter, 5);
        assertEquals(5, iter.head);
        assertEquals('l', iter.buf[iter.head]);
        IterImplForStreaming.skipFixedBytes(iter, 10);
        assertEquals(15, iter.head);
        assertEquals('"', iter.buf[iter.head]);
    }

    public void test_skipFixedBytes_IterImplForStreaming5() throws IOException {
        JsonIterator iter = JsonIterator.parse("{\"field\":\"hello\"}");
        IterImplForStreaming.skipFixedBytes(iter, 7);
        assertEquals(7, iter.head);
        assertEquals('"', iter.buf[iter.head]);
        IterImplForStreaming.skipFixedBytes(iter, 10);
        assertEquals(17, "{\"field\":\"hello\"}".length());
        assertEquals(17, iter.head);
        assertEquals(17, iter.buf.length);
    }

    //-----------------------------------------------------
    //-----------------------------------------------------
    //-----------------------------------------------------

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

    //-----------------------------------------------------
    //-----------------------------------------------------
    //-----------------------------------------------------

    public static class TestObject15 {
        @JsonProperty(defaultValueToOmit = "null")
        public Integer i1;
        @JsonProperty(defaultValueToOmit = "null")
        public Integer i2;
    }

    public void test_indention_with_empty_object() {
        Config config = JsoniterSpi.getCurrentConfig().copyBuilder()
                .indentionStep(2)
                .encodingMode(EncodingMode.REFLECTION_MODE)
                .build();
        assertEquals("{}", JsonStream.serialize(config, new TestObject15()));
        config = JsoniterSpi.getCurrentConfig().copyBuilder()
                .indentionStep(2)
                .encodingMode(EncodingMode.DYNAMIC_MODE)
                .build();
        assertEquals("{}", JsonStream.serialize(config, new TestObject15()));
    }

    public static class TestObject14 {
        @JsonProperty(nullable = true, defaultValueToOmit = "null")
        public String field1;
        @JsonProperty(nullable = false)
        public String field2;
        @JsonProperty(nullable = true, defaultValueToOmit = "void")
        public String field3;
    }

    public void test_indention() {
        Config dynamicCfg = new Config.Builder()
                .indentionStep(2)
                .encodingMode(EncodingMode.DYNAMIC_MODE)
                .build();
        TestObject14 obj = new TestObject14();
        obj.field1 = "1";
        obj.field2 = "2";
        String output = JsonStream.serialize(dynamicCfg, obj);
        assertEquals("{\n" +
                "  \"field1\": \"1\",\n" +
                "  \"field2\": \"2\",\n" +
                "  \"field3\": null\n" +
                "}", output);
        Config reflectionCfg = new Config.Builder()
                .indentionStep(2)
                .encodingMode(EncodingMode.REFLECTION_MODE)
                .build();
        output = JsonStream.serialize(reflectionCfg, obj);
        assertEquals("{\n" +
                "  \"field1\": \"1\",\n" +
                "  \"field2\": \"2\",\n" +
                "  \"field3\": null\n" +
                "}", output);
    }

    //-----------------------------------------------------
    //-----------------------------------------------------
    //-----------------------------------------------------

    private static class TestObject8 {
        public String field1;
        public long field2;

        public void setField1(String field1) {
            this.field1 = field1;
        }
    }

    public void test_updateBindingSetOp_CodegenImplObjectStrict() {
        ClassDescriptor desc = ClassDescriptor.getDecodingClassDescriptor(new ClassInfo(TestObject8.class), false);
        Binding binding = desc.fields.get(0);
        assertEquals("field1", CodegenImplObjectStrict.updateBindingSetOp(binding.name, binding));
        binding = desc.fields.get(1);
        assertEquals("field2", CodegenImplObjectStrict.updateBindingSetOp(binding.name, binding));
        binding = desc.setters.get(0);
        assertEquals("field1", CodegenImplObjectStrict.updateBindingSetOp(binding.name, binding));
    }

    public void test_updateBindingSetOp_CodegenImplObjectStrict2() {
        ClassDescriptor desc = ClassDescriptor.getDecodingClassDescriptor(new ClassInfo(TestObject8.class), false);
        String renderedField1 = "_field1_=\"value1\";";
        Binding binding = desc.fields.get(0);
        assertEquals("_field1_=\"value1\";", CodegenImplObjectStrict.updateBindingSetOp(renderedField1, binding));
        String renderedField2 = "_field2_=10;";
        binding = desc.fields.get(1);
        assertEquals("obj.field2=10;", CodegenImplObjectStrict.updateBindingSetOp(renderedField2, binding));
        binding = desc.setters.get(0);
        assertEquals("obj.setField1(\"value1\");", CodegenImplObjectStrict.updateBindingSetOp(renderedField1, binding));
    }

    public void test_updateBindingSetOp_CodegenImplObjectStrict3() {
        ClassDescriptor desc = ClassDescriptor.getDecodingClassDescriptor(new ClassInfo(TestObject8.class), false);
        String rendered = "String _field1_=\"value1\"; _field2_=10; _field1_=\"value2\";";
        Binding binding = desc.fields.get(0);
        assertEquals("String _field1_=\"value1\"; _field2_=10; _field1_=\"value2\";",
                CodegenImplObjectStrict.updateBindingSetOp(rendered, binding));
        binding = desc.fields.get(1);
        assertEquals("String _field1_=\"value1\"; obj.field2=10; _field1_=\"value2\";",
                CodegenImplObjectStrict.updateBindingSetOp(rendered, binding));
        binding = desc.setters.get(0);
        assertEquals("String obj.setField1(\"value1\"); _field2_=10; obj.setField1(\"value2\");",
                CodegenImplObjectStrict.updateBindingSetOp(rendered, binding));
    }

    //-----------------------------------------------------
    //-----------------------------------------------------
    //-----------------------------------------------------

    public void test_fillCacheUntil_ObjectLazyAny() {
        new TestObjectLazyAny().test_fillCacheUntil_ObjectLazyAny();
    }

    public void test_fillCacheUntil_ObjectLazyAny2() {
        new TestObjectLazyAny().test_fillCacheUntil_ObjectLazyAny2();
    }

    public void test_fillCacheUntil_ObjectLazyAny3() {
        new TestObjectLazyAny().test_fillCacheUntil_ObjectLazyAny3();
    }

    public void test_fillCacheUntil_ObjectLazyAny4() {
        new TestObjectLazyAny().test_fillCacheUntil_ObjectLazyAny4();
    }

    public void test_fillCacheUntil_ObjectLazyAny5() {
        new TestObjectLazyAny().test_fillCacheUntil_ObjectLazyAny5();
    }

    //-----------------------------------------------------
    //-----------------------------------------------------
    //-----------------------------------------------------

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

    //-----------------------------------------------------
    //-----------------------------------------------------
    //-----------------------------------------------------

    public void test_Double() {
        new TestBase64Float().test_Double();
    }

    public void test_double() {
        new TestBase64Float().test_double();
    }

    public void test_Float() {
        new TestBase64Float().test_Float();
    }

    public void test_float() {
        new TestBase64Float().test_float();
    }

    //-----------------------------------------------------
    //-----------------------------------------------------
    //-----------------------------------------------------

    public void test_read_any_IterImplForStreaming() {
        JsonIterator iter = JsonIterator.parse("[1,2,3]");
        try {
            Any any = IterImplForStreaming.readAny(iter);
            assertTrue(any.toBoolean());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    public void test_read_any_IterImplForStreaming2() {
        JsonIterator iter = JsonIterator.parse("[1,2,3]");
        try {
            Any any = IterImplForStreaming.readAny(iter);
            assertEquals(1, any.get(0).toInt());
            assertEquals(2, any.get(1).toInt());
            assertEquals(3, any.get(2).toInt());
        } catch (IOException e) {
            e.printStackTrace();
            fail();
        }
    }

    //-----------------------------------------------------
    //-----------------------------------------------------
    //-----------------------------------------------------

    public void test_get_ArrayLazyAny() {
        Any obj = JsonIterator.deserialize("[1,2,3]");
        assertEquals(1, obj.get(0).toInt());
        assertEquals(2, obj.get(1).toInt());
        assertEquals(3, obj.get(2).toInt());
    }

    public void test_get_ArrayLazyAny2() {
        Any obj = JsonIterator.deserialize("[1,2,3]");
        assertEquals(2, obj.get(1).toInt());
    }

    //-----------------------------------------------------
    //-----------------------------------------------------
    //-----------------------------------------------------

    public void test_no_decimal_float() throws IOException {
        stream.writeVal(100f);
        stream.close();
        assertEquals("100", baos.toString());
    }

    public void test_float2() throws IOException {
        stream.writeVal(0.000001f);
        stream.close();
        assertEquals("0.000001", baos.toString());
    }

    public void test_float3() throws IOException {
        stream.writeVal(0.00001f);
        stream.close();
        assertEquals("0.00001", baos.toString());
    }

    public void test_big_float() throws IOException {
        stream.writeVal((float) 0x4ffffff);
        stream.close();
        assertEquals("83886080", baos.toString());
    }

    //-----------------------------------------------------
    //-----------------------------------------------------
    //-----------------------------------------------------

    public void test_read_JsonIterator() throws IOException {
        JsonIterator iter = JsonIterator.parse("\"test\"");
        assertEquals("test", iter.read());
    }

    public void test_read_JsonIterator2() throws IOException {
        JsonIterator iter = JsonIterator.parse("100");
        assertEquals(100, iter.read());
    }

    public void test_read_JsonIterator3() throws IOException {
        JsonIterator iter = JsonIterator.parse("100.0");
        assertEquals(100d, iter.read());
    }

    public void test_read_JsonIterator4() throws IOException {
        JsonIterator iter = JsonIterator.parse("6643122506645376263");
        assertEquals(6643122506645376263L, iter.read());
    }

    public void test_read_JsonIterator5() throws IOException {
        JsonIterator iter = JsonIterator.parse("null");
        assertNull(iter.read());
    }

    public void test_read_JsonIterator6() throws IOException {
        JsonIterator iter = JsonIterator.parse("[1,2,3]");
        assertEquals(Arrays.asList(1, 2, 3), iter.read());
    }

    public void test_read_JsonIterator7() throws IOException {
        JsonIterator iter = JsonIterator.parse("{\"field1\":100}");
        assertEquals(100, ((Map) iter.read()).get("field1"));
    }

}
