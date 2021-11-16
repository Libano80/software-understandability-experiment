package com.jsoniter;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.FieldNamingStrategy;
import com.jsoniter.extra.GsonCompatibilityMode;
import com.jsoniter.spi.Binding;
import com.jsoniter.spi.ClassDescriptor;
import com.jsoniter.spi.ClassInfo;
import junit.framework.TestCase;

import java.io.IOException;
import java.lang.reflect.Field;
import java.text.DateFormat;

import static org.junit.Assert.assertNotEquals;

public class TestExperimentSet2 extends TestCase {

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

}
