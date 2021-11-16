package org.json.junit;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.XML;
import org.json.XMLTokener;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;

import static org.junit.Assert.*;

public class TestExperimentSet1 {

    /**
     * Check whether JSONObject handles large or high precision numbers correctly
     */
    @Test
    public void stringToValueNumbersTest() {
        assertTrue("-0 Should be a Double!", JSONObject.stringToValue("-0") instanceof Double);
        assertTrue("-0.0 Should be a Double!", JSONObject.stringToValue("-0.0") instanceof Double);
        assertTrue("'-' Should be a String!", JSONObject.stringToValue("-") instanceof String);
        assertTrue("0.2 should be a BigDecimal!",
                JSONObject.stringToValue("0.2") instanceof BigDecimal);
        assertTrue("Doubles should be BigDecimal, even when incorrectly converting floats!",
                JSONObject.stringToValue(new Double("0.2f").toString()) instanceof BigDecimal);
        /**
         * This test documents a need for BigDecimal conversion.
         */
        Object obj = JSONObject.stringToValue("299792.457999999984");
        assertTrue("does not evaluate to 299792.457999999984 BigDecimal!",
                obj.equals(new BigDecimal("299792.457999999984")));
        assertTrue("1 should be an Integer!",
                JSONObject.stringToValue("1") instanceof Integer);
        assertTrue("Integer.MAX_VALUE should still be an Integer!",
                JSONObject.stringToValue(new Integer(Integer.MAX_VALUE).toString()) instanceof Integer);
        assertTrue("Large integers should be a Long!",
                JSONObject.stringToValue(Long.valueOf(((long) Integer.MAX_VALUE) + 1).toString()) instanceof Long);
        assertTrue("Long.MAX_VALUE should still be an Integer!",
                JSONObject.stringToValue(new Long(Long.MAX_VALUE).toString()) instanceof Long);

        String str = new BigInteger(new Long(Long.MAX_VALUE).toString()).add(BigInteger.ONE).toString();
        assertTrue("Really large integers currently evaluate to BigInteger",
                JSONObject.stringToValue(str).equals(new BigInteger("9223372036854775808")));
    }

    /**
     * Exercise the JSONObject write(Writer, int, int) method
     */
    @Test
    public void write3Param() throws IOException {
        String str0 = "{\"key1\":\"value1\",\"key2\":[1,false,3.14]}";
        String str2 =
                "{\n" +
                        "   \"key1\": \"value1\",\n" +
                        "   \"key2\": [\n" +
                        "     1,\n" +
                        "     false,\n" +
                        "     3.14\n" +
                        "   ]\n" +
                        " }";
        JSONObject jsonObject = new JSONObject(str0);
        StringWriter stringWriter = new StringWriter();
        try {
            String actualStr = jsonObject.write(stringWriter, 0, 0).toString();

            assertEquals("length", str0.length(), actualStr.length());
            assertTrue("key1", actualStr.contains("\"key1\":\"value1\""));
            assertTrue("key2", actualStr.contains("\"key2\":[1,false,3.14]"));
        } finally {
            try {
                stringWriter.close();
            } catch (Exception e) {
            }
        }

        stringWriter = new StringWriter();
        try {
            String actualStr = jsonObject.write(stringWriter, 2, 1).toString();

            assertEquals("length", str2.length(), actualStr.length());
            assertTrue("key1", actualStr.contains("   \"key1\": \"value1\""));
            assertTrue("key2", actualStr.contains("   \"key2\": [\n" +
                    "     1,\n" +
                    "     false,\n" +
                    "     3.14\n" +
                    "   ]")
            );
        } finally {
            try {
                stringWriter.close();
            } catch (Exception e) {
            }
        }
    }

    /**
     * Verifies that the optBigInteger method properly converts values to BigInteger and coerce them consistently.
     */
    @Test
    public void jsonObjectOptBigInteger() {
        JSONObject jo = new JSONObject().put("int", 123).put("long", 654L)
                .put("float", 1.234f).put("double", 2.345d)
                .put("bigInteger", new BigInteger("1234"))
                .put("bigDecimal", new BigDecimal("1234.56789"))
                .put("nullVal", JSONObject.NULL);

        assertEquals(new BigInteger("123"), jo.optBigInteger("int", null));
        assertEquals(new BigInteger("654"), jo.optBigInteger("long", null));
        assertEquals(new BigInteger("1"), jo.optBigInteger("float", null));
        assertEquals(new BigInteger("2"), jo.optBigInteger("double", null));
        assertEquals(new BigInteger("1234"), jo.optBigInteger("bigInteger", null));
        assertEquals(new BigInteger("1234"), jo.optBigInteger("bigDecimal", null));
        assertNull(jo.optBigDecimal("nullVal", null));
    }

    @Test
    public void testNextMeta() {
        String xmlStr = "  xml";
        XMLTokener xmlTokener = new XMLTokener(xmlStr);
        try {
            xmlTokener.nextMeta();
            fail();
        } catch (JSONException e) {
            assertEquals("Unterminated string at 5 [character 6 line 1]", e.getMessage());
        }
    }

    @Test
    public void testNextMeta2() {
        String xmlStr = "<xml/>";
        XMLTokener xmlTokener = new XMLTokener(xmlStr);
        assertEquals(XML.LT, xmlTokener.nextMeta());
        assertEquals(Boolean.TRUE, xmlTokener.nextMeta());
        assertEquals(XML.SLASH, xmlTokener.nextMeta());
        assertEquals(XML.GT, xmlTokener.nextMeta());
    }

    @Test
    public void testNextMeta3() {
        String xmlStr = "<>/=?!\"test\"'test'";
        XMLTokener xmlTokener = new XMLTokener(xmlStr);
        assertEquals(XML.LT, xmlTokener.nextMeta());
        assertEquals(XML.GT, xmlTokener.nextMeta());
        assertEquals(XML.SLASH, xmlTokener.nextMeta());
        assertEquals(XML.EQ, xmlTokener.nextMeta());
        assertEquals(XML.QUEST, xmlTokener.nextMeta());
        assertEquals(XML.BANG, xmlTokener.nextMeta());
        assertEquals(Boolean.TRUE, xmlTokener.nextMeta());
        assertEquals(Boolean.TRUE, xmlTokener.nextMeta());
        try {
            xmlTokener.nextMeta();
            fail();
        } catch (JSONException e) {
            assertEquals("Misshaped meta tag at 18 [character 19 line 1]", e.getMessage());
        }
    }

}
