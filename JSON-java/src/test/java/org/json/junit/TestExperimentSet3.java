package org.json.junit;

import org.json.*;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.Assert.*;

public class TestExperimentSet3 {

    @Test
    public void testNextToken() {
        String xmlStr = "  xml ";
        XMLTokener xmlTokener = new XMLTokener(xmlStr);
        assertEquals("xml", xmlTokener.nextToken());
    }

    @Test
    public void testNextToken2() {
        String xmlStr = "\"test\"'test'";
        XMLTokener xmlTokener = new XMLTokener(xmlStr);
        assertEquals("test", xmlTokener.nextToken());
        assertEquals("test", xmlTokener.nextToken());
    }

    @Test
    public void testNextToken3() {
        String xmlStr = "<xml>";
        XMLTokener xmlTokener = new XMLTokener(xmlStr);
        try {
            xmlTokener.nextToken();
        } catch (JSONException e) {
            assertEquals("Misplaced '<' at 1 [character 2 line 1]", e.getMessage());
        }
    }

    @Test
    public void testNextToken4() {
        String xmlStr = ">/=?!\"test\"'test'";
        XMLTokener xmlTokener = new XMLTokener(xmlStr);
        assertEquals(XML.GT, xmlTokener.nextToken());
        assertEquals(XML.SLASH, xmlTokener.nextToken());
        assertEquals(XML.EQ, xmlTokener.nextToken());
        assertEquals(XML.QUEST, xmlTokener.nextToken());
        assertEquals(XML.BANG, xmlTokener.nextToken());
        assertEquals("test", xmlTokener.nextToken());
        assertEquals("test", xmlTokener.nextToken());
        try {
            xmlTokener.nextToken();
            fail();
        } catch (JSONException e) {
            assertEquals("Misshaped element at 17 [character 18 line 1]", e.getMessage());
        }
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
     * test to validate certain conditions of XML unescaping.
     */
    @Test
    public void testUnescape() {
        assertEquals("{\"xml\":\"Can cope <;\"}",
                XML.toJSONObject("<xml>Can cope &lt;; </xml>").toString());
        assertEquals("Can cope <; ", XML.unescape("Can cope &lt;; "));

        assertEquals("{\"xml\":\"Can cope & ;\"}",
                XML.toJSONObject("<xml>Can cope &amp; ; </xml>").toString());
        assertEquals("Can cope & ; ", XML.unescape("Can cope &amp; ; "));

        assertEquals("{\"xml\":\"Can cope &;\"}",
                XML.toJSONObject("<xml>Can cope &amp;; </xml>").toString());
        assertEquals("Can cope &; ", XML.unescape("Can cope &amp;; "));

        // unicode entity
        assertEquals("{\"xml\":\"Can cope 4;\"}",
                XML.toJSONObject("<xml>Can cope &#x34;; </xml>").toString());
        assertEquals("Can cope 4; ", XML.unescape("Can cope &#x34;; "));

        // double escaped
        assertEquals("{\"xml\":\"Can cope &lt;\"}",
                XML.toJSONObject("<xml>Can cope &amp;lt; </xml>").toString());
        assertEquals("Can cope &lt; ", XML.unescape("Can cope &amp;lt; "));

        assertEquals("{\"xml\":\"Can cope &#x34;\"}",
                XML.toJSONObject("<xml>Can cope &amp;#x34; </xml>").toString());
        assertEquals("Can cope &#x34; ", XML.unescape("Can cope &amp;#x34; "));

    }

    /**
     * Tests to verify that supported escapes in XML are converted to actual values.
     */
    @Test
    public void testIssue537CaseSensitiveHexUnEscapeDirect() {
        String origStr =
                "Neutrophils.Hypersegmented &#X7C; Bld-Ser-Plas";
        String expectedStr =
                "Neutrophils.Hypersegmented | Bld-Ser-Plas";
        String actualStr = XML.unescape(origStr);

        assertEquals("Case insensitive Entity unescape", expectedStr, actualStr);
    }

    private void checkValid(String testStr, Class<?> aClass) {
        Object result = nextValue(testStr);

        // Check class of object returned
        if (null == aClass) {
            if (JSONObject.NULL.equals(result)) {
                // OK
            } else {
                throw new JSONException("Unexpected class: " + result.getClass().getSimpleName());
            }
        } else {
            if (null == result) {
                throw new JSONException("Unexpected null result");
            } else if (!aClass.isAssignableFrom(result.getClass())) {
                throw new JSONException("Unexpected class: " + result.getClass().getSimpleName());
            }
        }

    }

    /**
     * Verifies that JSONTokener can read a stream that contains a value. After
     * the reading is done, check that the stream is left in the correct state
     * by reading the characters after. All valid cases should reach end of stream.
     *
     * @param testStr
     * @return
     * @throws Exception
     */
    private Object nextValue(String testStr) throws JSONException {
        StringReader sr = new StringReader(testStr);
        try {
            JSONTokener tokener = new JSONTokener(sr);

            Object result = tokener.nextValue();

            if (result == null) {
                throw new JSONException("Unable to find value token in JSON stream: (" + tokener + "): " + testStr);
            }

            char c = tokener.nextClean();
            if (0 != c) {
                throw new JSONException("Unexpected character found at end of JSON stream: " + c + " (" + tokener + "): " + testStr);
            }

            return result;
        } finally {
            sr.close();
        }

    }

    @Test
    public void testValid() {
        checkValid("0", Number.class);
        checkValid(" 0  ", Number.class);
        checkValid("23", Number.class);
        checkValid("23.5", Number.class);
        checkValid(" 23.5  ", Number.class);
        checkValid("null", null);
        checkValid(" null  ", null);
        checkValid("true", Boolean.class);
        checkValid(" true\n", Boolean.class);
        checkValid("false", Boolean.class);
        checkValid("\nfalse  ", Boolean.class);
        checkValid("{}", JSONObject.class);
        checkValid(" {}  ", JSONObject.class);
        checkValid("{\"a\":1}", JSONObject.class);
        checkValid(" {\"a\":1}  ", JSONObject.class);
        checkValid("[]", JSONArray.class);
        checkValid(" []  ", JSONArray.class);
        checkValid("[1,2]", JSONArray.class);
        checkValid("\n\n[1,2]\n\n", JSONArray.class);
        checkValid("1 2", String.class);
    }

}
