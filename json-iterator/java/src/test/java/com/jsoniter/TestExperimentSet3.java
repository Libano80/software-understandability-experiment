package com.jsoniter;

import com.jsoniter.any.Any;
import com.jsoniter.extra.GsonCompatibilityMode;
import com.jsoniter.output.JsonStream;
import com.jsoniter.spi.Binding;
import com.jsoniter.spi.ClassDescriptor;
import com.jsoniter.spi.ClassInfo;
import com.jsoniter.spi.Decoder;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;

public class TestExperimentSet3 extends TestCase {

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

    private ByteArrayOutputStream baos;
    private JsonStream stream;

    public void setUp() {
        baos = new ByteArrayOutputStream();
        stream = new JsonStream(baos, 4096);
    }

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

}
