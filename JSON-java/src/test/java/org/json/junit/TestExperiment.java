package org.json.junit;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import org.json.*;
import org.json.junit.data.*;
import org.junit.Test;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestExperiment {

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

    //-----------------------------------------------------------------
    //-----------------------------------------------------------------
    //-----------------------------------------------------------------

    /**
     * JSON string cannot be reverted to original xml when type guessing is used.
     */
    @Test
    public void testToJSONArray_reversibility() {
        final String originalXml = "<root><id>01</id><id>1</id><id>00</id><id>0</id><item id=\"01\"/><title>True</title></root>";
        final String revertedXml = JSONML.toString(JSONML.toJSONArray(originalXml, false));
        assertNotEquals(revertedXml, originalXml);
    }

    /**
     * JSON string cannot be reverted to original xml when type guessing is used.
     * When we force all the values as string, the original text comes back.
     */
    @Test
    public void testToJSONArray_reversibility2() {
        final String originalXml = "<root><id>01</id><id>1</id><id>00</id><id>0</id><item id=\"01\"/><title>True</title></root>";
        final String expectedJsonString = "[\"root\",[\"id\",\"01\"],[\"id\",\"1\"],[\"id\",\"00\"],[\"id\",\"0\"],[\"item\",{\"id\":\"01\"}],[\"title\",\"True\"]]";
        final JSONArray json = JSONML.toJSONArray(originalXml, true);
        assertEquals(expectedJsonString, json.toString());

        final String reverseXml = JSONML.toString(json);
        assertEquals(originalXml, reverseXml);
    }

    /**
     * JSON can be reverted to original xml.
     */
    @Test
    public void testToJSONArray_reversibility3() {
        final String originalXml = "<readResult><errors someAttr=\"arrtValue\"><code>400</code></errors><errors><code>402</code></errors></readResult>";
        final JSONArray jsonArray = JSONML.toJSONArray(originalXml, false);
        final String revertedXml = JSONML.toString(jsonArray);
        assertEquals(revertedXml, originalXml);
    }

    /**
     * Convert an XML document into a JSONArray, then use JSONML.toString()
     * to convert it into a string. This string is then converted back into
     * a JSONArray. Both JSONArrays are compared against a control to
     * confirm the contents.
     */
    @Test
    public void toJSONArray() {
        /**
         * xmlStr contains XML text which is transformed into a JSONArray.
         * Each element becomes a JSONArray:
         * 1st entry = elementname
         * 2nd entry = attributes object (if present)
         * 3rd entry = content (if present)
         * 4th entry = child element JSONArrays (if present)
         * The result is compared against an expected JSONArray.
         * The transformed JSONArray is then transformed back into a string
         * which is used to create a final JSONArray, which is also compared
         * against the expected JSONArray.
         */
        String xmlStr =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<addresses xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"" +
                        "xsi:noNamespaceSchemaLocation='test.xsd'>\n" +
                        "<address attr1=\"attrValue1\" attr2=\"attrValue2\" attr3=\"attrValue3\">\n" +
                        "<name nameType=\"mine\">myName</name>\n" +
                        "<nocontent/>>\n" +
                        "</address>\n" +
                        "</addresses>";
        String expectedStr =
                "[\"addresses\"," +
                        "{\"xsi:noNamespaceSchemaLocation\":\"test.xsd\"," +
                        "\"xmlns:xsi\":\"http://www.w3.org/2001/XMLSchema-instance\"}," +
                        "[\"address\"," +
                        "{\"attr1\":\"attrValue1\",\"attr2\":\"attrValue2\",\"attr3\":\"attrValue3\"}," +
                        "[\"name\", {\"nameType\":\"mine\"},\"myName\"]," +
                        "[\"nocontent\"]," +
                        "\">\"" +
                        "]" +
                        "]";
        JSONArray jsonArray = JSONML.toJSONArray(xmlStr);
        JSONArray expectedJsonArray = new JSONArray(expectedStr);
        String xmlToStr = JSONML.toString(jsonArray);
        JSONArray finalJsonArray = JSONML.toJSONArray(xmlToStr);
        Util.compareActualVsExpectedJsonArrays(jsonArray, expectedJsonArray);
        Util.compareActualVsExpectedJsonArrays(finalJsonArray, expectedJsonArray);
    }

    //-----------------------------------------------------------------
    //-----------------------------------------------------------------
    //-----------------------------------------------------------------

    /**
     * Tests Number serialization.
     */
    @Test
    public void verifyNumberOutput() {
        /**
         * MyNumberContainer is a POJO, so call JSONObject(bean),
         * which builds a map of getter names/values
         * The only getter is getMyNumber (key=myNumber),
         * whose return value is MyNumber. MyNumber extends Number,
         * but is not recognized as such by wrap() per current
         * implementation, so wrap() returns the default new JSONObject(bean).
         * The only getter is getNumber (key=number), whose return value is
         * BigDecimal(42).
         */
        JSONObject jsonObject = new JSONObject(new MyNumberContainer());
        String actual = jsonObject.toString();
        String expected = "{\"myNumber\":{\"number\":42}}";
        assertEquals("Equal", expected, actual);

        /**
         * JSONObject.put() handles objects differently than the
         * bean constructor. Where the bean ctor wraps objects before
         * placing them in the map, put() inserts the object without wrapping.
         * In this case, a MyNumber instance is the value.
         * The MyNumber.toString() method is responsible for
         * returning a reasonable value: the string '42'.
         */
        jsonObject = new JSONObject();
        jsonObject.put("myNumber", new MyNumber());
        actual = jsonObject.toString();
        expected = "{\"myNumber\":42}";
        assertEquals("Equal", expected, actual);

        /**
         * Calls the JSONObject(Map) ctor, which calls wrap() for values.
         * AtomicInteger is a Number, but is not recognized by wrap(), per
         * current implementation. However, the type is
         * 'java.util.concurrent.atomic', so due to the 'java' prefix,
         * wrap() inserts the value as a string. That is why 42 comes back
         * wrapped in quotes.
         */
        jsonObject = new JSONObject(Collections.singletonMap("myNumber", new AtomicInteger(42)));
        actual = jsonObject.toString();
        expected = "{\"myNumber\":\"42\"}";
        assertEquals("Equal", expected, actual);

        /**
         * JSONObject.put() inserts the AtomicInteger directly into the
         * map not calling wrap(). In toString()->write()->writeValue(),
         * AtomicInteger is recognized as a Number, and converted via
         * numberToString() into the unquoted string '42'.
         */
        jsonObject = new JSONObject();
        jsonObject.put("myNumber", new AtomicInteger(42));
        actual = jsonObject.toString();
        expected = "{\"myNumber\":42}";
        assertEquals("Equal", expected, actual);

        /**
         * Calls the JSONObject(Map) ctor, which calls wrap() for values.
         * Fraction is a Number, but is not recognized by wrap(), per
         * current implementation. As a POJO, Fraction is handled as a
         * bean and inserted into a contained JSONObject. It has 2 getters,
         * for numerator and denominator.
         */
        jsonObject = new JSONObject(Collections.singletonMap("myNumber", new Fraction(4, 2)));
        assertEquals(1, jsonObject.length());
        assertEquals(2, ((JSONObject) (jsonObject.get("myNumber"))).length());
        assertEquals("Numerator", BigInteger.valueOf(4), jsonObject.query("/myNumber/numerator"));
        assertEquals("Denominator", BigInteger.valueOf(2), jsonObject.query("/myNumber/denominator"));

        /**
         * JSONObject.put() inserts the Fraction directly into the
         * map not calling wrap(). In toString()->write()->writeValue(),
         * Fraction is recognized as a Number, and converted via
         * numberToString() into the unquoted string '4/2'. But the
         * BigDecimal sanity check fails, so writeValue() defaults
         * to returning a safe JSON quoted string. Pretty slick!
         */
        jsonObject = new JSONObject();
        jsonObject.put("myNumber", new Fraction(4, 2));
        actual = jsonObject.toString();
        expected = "{\"myNumber\":\"4/2\"}"; // valid JSON, bug fixed
        assertEquals("Equal", expected, actual);
    }

    /**
     * test that validates a singleton can be serialized as a bean.
     */
    @SuppressWarnings("boxing")
    @Test
    public void testSingletonEnumBean() {
        final JSONObject jo = new JSONObject(SingletonEnum.getInstance());
        assertEquals(jo.keySet().toString(), 1, jo.length());
        assertEquals(0, jo.get("someInt"));
        assertEquals(null, jo.opt("someString"));

        // Update the singleton values
        SingletonEnum.getInstance().setSomeInt(42);
        SingletonEnum.getInstance().setSomeString("Something");
        final JSONObject jo2 = new JSONObject(SingletonEnum.getInstance());
        assertEquals(2, jo2.length());
        assertEquals(42, jo2.get("someInt"));
        assertEquals("Something", jo2.get("someString"));

        // ensure our original jo hasn't changed.
        assertEquals(0, jo.get("someInt"));
        assertEquals(null, jo.opt("someString"));
    }

    //-----------------------------------------------------------------
    //-----------------------------------------------------------------
    //-----------------------------------------------------------------

    /**
     * Store a cookie with all of the supported attributes in a
     * JSONObject. The secure attribute, which has no value, is treated
     * as a boolean.
     */
    @Test
    public void multiPartCookie() {
        String cookieStr =
                "PH=deleted;  " +
                        " expires=Wed, 19-Mar-2014 17:53:53 GMT;" +
                        "path=/;   " +
                        "    domain=.yahoo.com;" +
                        "secure";
        String expectedCookieStr =
                "{" +
                        "\"name\":\"PH\"," +
                        "\"value\":\"deleted\"," +
                        "\"path\":\"/\"," +
                        "\"expires\":\"Wed, 19-Mar-2014 17:53:53 GMT\"," +
                        "\"domain\":\".yahoo.com\"," +
                        "\"secure\":true" +
                        "}";
        JSONObject jsonObject = Cookie.toJSONObject(cookieStr);
        JSONObject expectedJsonObject = new JSONObject(expectedCookieStr);
        Util.compareActualVsExpectedJsonObjects(jsonObject, expectedJsonObject);
    }

    /**
     * Store a cookie with all of the supported attributes in a
     * JSONObject. The secure attribute, which has no value, is treated
     * as a boolean.
     */
    @Test
    public void multiPartCookie2() {
        String cookieStr =
                "PH=deleted;  " +
                        " expires=Wed, 19-Mar-2014 17:53:53 GMT;" +
                        "path=/;   " +
                        "secure;" +
                        "domain=.yahoo.com";
        String expectedCookieStr =
                "{" +
                        "\"name\":\"PH\"," +
                        "\"value\":\"deleted\"," +
                        "\"path\":\"/\"," +
                        "\"expires\":\"Wed, 19-Mar-2014 17:53:53 GMT\"," +
                        "\"secure\":true," +
                        "\"domain\":\".yahoo.com\"" +
                        "}";
        JSONObject jsonObject = Cookie.toJSONObject(cookieStr);
        JSONObject expectedJsonObject = new JSONObject(expectedCookieStr);
        Util.compareActualVsExpectedJsonObjects(jsonObject, expectedJsonObject);
    }

    /**
     * Store a cookie with all of the supported attributes in a
     * JSONObject. Both the secure and the flag2 attributes, which have no value, are treated
     * as booleans.
     */
    @Test
    public void multiPartCookieDoubleFlag() {
        String cookieStr =
                "PH=deleted;  " +
                        " expires=Wed, 19-Mar-2014 17:53:53 GMT;" +
                        "path=/;   " +
                        "    domain=.yahoo.com;" +
                        "secure;" +
                        "flag2";
        String expectedCookieStr =
                "{" +
                        "\"name\":\"PH\"," +
                        "\"value\":\"deleted\"," +
                        "\"path\":\"/\"," +
                        "\"expires\":\"Wed, 19-Mar-2014 17:53:53 GMT\"," +
                        "\"domain\":\".yahoo.com\"," +
                        "\"secure\":true," +
                        "\"flag2\":true" +
                        "}";
        JSONObject jsonObject = Cookie.toJSONObject(cookieStr);
        JSONObject expectedJsonObject = new JSONObject(expectedCookieStr);
        Util.compareActualVsExpectedJsonObjects(jsonObject, expectedJsonObject);
    }

    //-----------------------------------------------------------------
    //-----------------------------------------------------------------
    //-----------------------------------------------------------------

    /**
     * A string may be URL-encoded when converting to JSONObject.
     * If found, '+' is converted to ' ', and %hh hex strings are converted
     * to their ascii char equivalents. This test confirms the decoding
     * behavior.
     */
    @Test
    public void convertEncodedCookieToString() {
        String cookieStr =
                "PH=deleted;  " +
                        " expires=Wed,+19-Mar-2014+17:53:53+GMT;" +
                        "path=/%2Bthis/is%26/a/spec%3Bsegment%3D;   " +
                        "    domain=.yahoo.com;" +
                        "secure";
        String expectedCookieStr =
                "{\"path\":\"/+this/is&/a/spec;segment=\"," +
                        "\"expires\":\"Wed, 19-Mar-2014 17:53:53 GMT\"," +
                        "\"domain\":\".yahoo.com\"," +
                        "\"name\":\"PH\"," +
                        "\"secure\":true," +
                        "\"value\":\"deleted\"}";
        JSONObject jsonObject = Cookie.toJSONObject(cookieStr);
        JSONObject expectedJsonObject = new JSONObject(expectedCookieStr);
        String cookieToStr = Cookie.toString(jsonObject);
        JSONObject finalJsonObject = Cookie.toJSONObject(cookieToStr);
        Util.compareActualVsExpectedJsonObjects(jsonObject, expectedJsonObject);
        Util.compareActualVsExpectedJsonObjects(finalJsonObject, expectedJsonObject);
    }

    /**
     * A public API method performs URL decoding for strings.
     * '+' is converted to space and %hh hex strings are converted to
     * their ascii equivalent values. The string is not trimmed.
     * This test confirms that behavior.
     */
    @Test
    public void unescapeString() {
        String str = " +%2b%25%0d%0a%09%08%25%3d%3b%3b%3b+ ";
        String expectedStr = "  +%\r\n\t\b%=;;;  ";
        String actualStr = Cookie.unescape(str);
        assertTrue("expect unescape() to decode correctly. Actual: " + actualStr +
                " expected: " + expectedStr, expectedStr.equals(actualStr));
    }

    //-----------------------------------------------------------------
    //-----------------------------------------------------------------
    //-----------------------------------------------------------------

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

    //-----------------------------------------------------------------
    //-----------------------------------------------------------------
    //-----------------------------------------------------------------

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

    //-----------------------------------------------------------------
    //-----------------------------------------------------------------
    //-----------------------------------------------------------------

    /**
     * Exercises the JSONObject.valueToString() method for various types
     */
    @Test
    public void valueToString() {

        assertTrue("null valueToString() incorrect",
                "null".equals(JSONObject.valueToString(null)));
        MyJsonString jsonString = new MyJsonString();
        assertTrue("jsonstring valueToString() incorrect",
                "my string".equals(JSONObject.valueToString(jsonString)));
        assertTrue("boolean valueToString() incorrect",
                "true".equals(JSONObject.valueToString(Boolean.TRUE)));
        assertTrue("non-numeric double",
                "null".equals(JSONObject.doubleToString(Double.POSITIVE_INFINITY)));
        String jsonObjectStr =
                "{" +
                        "\"key1\":\"val1\"," +
                        "\"key2\":\"val2\"," +
                        "\"key3\":\"val3\"" +
                        "}";
        JSONObject jsonObject = new JSONObject(jsonObjectStr);
        assertTrue("jsonObject valueToString() incorrect",
                JSONObject.valueToString(jsonObject).equals(jsonObject.toString()));
        String jsonArrayStr =
                "[1,2,3]";
        JSONArray jsonArray = new JSONArray(jsonArrayStr);
        assertTrue("jsonArray valueToString() incorrect",
                JSONObject.valueToString(jsonArray).equals(jsonArray.toString()));
        Map<String, String> map = new HashMap<String, String>();
        map.put("key1", "val1");
        map.put("key2", "val2");
        map.put("key3", "val3");
        assertTrue("map valueToString() incorrect",
                jsonObject.toString().equals(JSONObject.valueToString(map)));
        Collection<Integer> collection = new ArrayList<Integer>();
        collection.add(new Integer(1));
        collection.add(new Integer(2));
        collection.add(new Integer(3));
        assertTrue("collection valueToString() expected: " +
                        jsonArray.toString() + " actual: " +
                        JSONObject.valueToString(collection),
                jsonArray.toString().equals(JSONObject.valueToString(collection)));
        Integer[] array = {new Integer(1), new Integer(2), new Integer(3)};
        assertTrue("array valueToString() incorrect",
                jsonArray.toString().equals(JSONObject.valueToString(array)));
    }

    //-----------------------------------------------------------------
    //-----------------------------------------------------------------
    //-----------------------------------------------------------------

    /**
     * JSONObject built from a bean. In this case all but one of the
     * bean getters return valid JSON types
     */
    @SuppressWarnings("boxing")
    @Test
    public void jsonObjectByBean1() {
        /**
         * Default access classes have to be mocked since JSONObject, which is
         * not in the same package, cannot call MyBean methods by reflection.
         */
        MyBean myBean = mock(MyBean.class);
        when(myBean.getDoubleKey()).thenReturn(-23.45e7);
        when(myBean.getIntKey()).thenReturn(42);
        when(myBean.getStringKey()).thenReturn("hello world!");
        when(myBean.getEscapeStringKey()).thenReturn("h\be\tllo w\u1234orld!");
        when(myBean.isTrueKey()).thenReturn(true);
        when(myBean.isFalseKey()).thenReturn(false);
        when(myBean.getStringReaderKey()).thenReturn(
                new StringReader("") {
                });

        JSONObject jsonObject = new JSONObject(myBean);

        // validate JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonObject.toString());
        assertTrue("expected 8 top level items", ((Map<?, ?>) (JsonPath.read(doc, "$"))).size() == 8);
        assertTrue("expected 0 items in stringReaderKey", ((Map<?, ?>) (JsonPath.read(doc, "$.stringReaderKey"))).size() == 0);
        assertTrue("expected true", Boolean.TRUE.equals(jsonObject.query("/trueKey")));
        assertTrue("expected false", Boolean.FALSE.equals(jsonObject.query("/falseKey")));
        assertTrue("expected hello world!", "hello world!".equals(jsonObject.query("/stringKey")));
        assertTrue("expected h\be\tllo w\u1234orld!", "h\be\tllo w\u1234orld!".equals(jsonObject.query("/escapeStringKey")));
        assertTrue("expected 42", Integer.valueOf("42").equals(jsonObject.query("/intKey")));
        assertTrue("expected -23.45e7", Double.valueOf("-23.45e7").equals(jsonObject.query("/doubleKey")));
        // sorry, mockito artifact
        assertTrue("expected 2 callbacks items", ((List<?>) (JsonPath.read(doc, "$.callbacks"))).size() == 2);
        assertTrue("expected 0 handler items", ((Map<?, ?>) (JsonPath.read(doc, "$.callbacks[0].handler"))).size() == 0);
        assertTrue("expected 0 callbacks[1] items", ((Map<?, ?>) (JsonPath.read(doc, "$.callbacks[1]"))).size() == 0);
    }

    /**
     * JSONObjects can be built from a Map<String, Object>.
     * In this test all of the map entries are valid JSON types.
     */
    @Test
    public void jsonObjectByMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("trueKey", new Boolean(true));
        map.put("falseKey", new Boolean(false));
        map.put("stringKey", "hello world!");
        map.put("escapeStringKey", "h\be\tllo w\u1234orld!");
        map.put("intKey", new Long(42));
        map.put("doubleKey", new Double(-23.45e67));
        JSONObject jsonObject = new JSONObject(map);

        // validate JSON
        Object doc = Configuration.defaultConfiguration().jsonProvider().parse(jsonObject.toString());
        assertTrue("expected 6 top level items", ((Map<?, ?>) (JsonPath.read(doc, "$"))).size() == 6);
        assertTrue("expected \"trueKey\":true", Boolean.TRUE.equals(jsonObject.query("/trueKey")));
        assertTrue("expected \"falseKey\":false", Boolean.FALSE.equals(jsonObject.query("/falseKey")));
        assertTrue("expected \"stringKey\":\"hello world!\"", "hello world!".equals(jsonObject.query("/stringKey")));
        assertTrue("expected \"escapeStringKey\":\"h\be\tllo w\u1234orld!\"", "h\be\tllo w\u1234orld!".equals(jsonObject.query("/escapeStringKey")));
        assertTrue("expected \"doubleKey\":-23.45e67", Double.valueOf("-23.45e67").equals(jsonObject.query("/doubleKey")));
    }

    //-----------------------------------------------------------------
    //-----------------------------------------------------------------
    //-----------------------------------------------------------------

    /**
     * Tests that the similar method is working as expected.
     */
    @Test
    public void verifySimilar() {
        final String string1 = "HasSameRef";
        final String string2 = "HasDifferentRef";
        JSONObject obj1 = new JSONObject()
                .put("key1", "abc")
                .put("key2", 2)
                .put("key3", string1);

        JSONObject obj2 = new JSONObject()
                .put("key1", "abc")
                .put("key2", 3)
                .put("key3", string1);

        JSONObject obj3 = new JSONObject()
                .put("key1", "abc")
                .put("key2", 2)
                .put("key3", new String(string1));

        JSONObject obj4 = new JSONObject()
                .put("key1", "abc")
                .put("key2", 2.0)
                .put("key3", new String(string1));

        JSONObject obj5 = new JSONObject()
                .put("key1", "abc")
                .put("key2", 2.0)
                .put("key3", new String(string2));

        assertFalse("obj1-obj2 Should eval to false", obj1.similar(obj2));
        assertTrue("obj1-obj3 Should eval to true", obj1.similar(obj3));
        assertTrue("obj1-obj4 Should eval to true", obj1.similar(obj4));
        assertFalse("obj1-obj5 Should eval to false", obj1.similar(obj5));
        // verify that a double and big decimal are "similar"
        assertTrue("should eval to true", new JSONObject().put("a", 1.1d).similar(new JSONObject("{\"a\":1.1}")));
        // Confirm #618 is fixed (compare should not exit early if similar numbers are found)
        // Note that this test may not work if the JSONObject map entry order changes
        JSONObject first = new JSONObject("{\"a\": 1, \"b\": 2, \"c\": 3}");
        JSONObject second = new JSONObject("{\"a\": 1, \"b\": 2.0, \"c\": 4}");
        assertFalse("first-second should eval to false", first.similar(second));
    }

    //-----------------------------------------------------------------
    //-----------------------------------------------------------------
    //-----------------------------------------------------------------

    /**
     * Create a JSONArray doc with a variety of different elements.
     * Confirm that the values can be accessed via the get[type]() API methods
     */
    @SuppressWarnings("boxing")
    @Test
    public void getArrayValues() {
        String arrayStr =
                "[" +
                        "true," +
                        "false," +
                        "\"true\"," +
                        "\"false\"," +
                        "\"hello\"," +
                        "23.45e-4," +
                        "\"23.45\"," +
                        "42," +
                        "\"43\"," +
                        "[" +
                        "\"world\"" +
                        "]," +
                        "{" +
                        "\"key1\":\"value1\"," +
                        "\"key2\":\"value2\"," +
                        "\"key3\":\"value3\"," +
                        "\"key4\":\"value4\"" +
                        "}," +
                        "0," +
                        "\"-1\"" +
                        "]";

        JSONArray jsonArray = new JSONArray(arrayStr);
        // booleans
        assertTrue("Array true",
                true == jsonArray.getBoolean(0));
        assertTrue("Array false",
                false == jsonArray.getBoolean(1));
        assertTrue("Array string true",
                true == jsonArray.getBoolean(2));
        assertTrue("Array string false",
                false == jsonArray.getBoolean(3));
        // strings
        assertTrue("Array value string",
                "hello".equals(jsonArray.getString(4)));
        // doubles
        assertTrue("Array double",
                new Double(23.45e-4).equals(jsonArray.getDouble(5)));
        assertTrue("Array string double",
                new Double(23.45).equals(jsonArray.getDouble(6)));
        assertTrue("Array double can be float",
                new Float(23.45e-4f).equals(jsonArray.getFloat(5)));
        // ints
        assertTrue("Array value int",
                new Integer(42).equals(jsonArray.getInt(7)));
        assertTrue("Array value string int",
                new Integer(43).equals(jsonArray.getInt(8)));
        // nested objects
        JSONArray nestedJsonArray = jsonArray.getJSONArray(9);
        assertTrue("Array value JSONArray", nestedJsonArray != null);
        JSONObject nestedJsonObject = jsonArray.getJSONObject(10);
        assertTrue("Array value JSONObject", nestedJsonObject != null);
        // longs
        assertTrue("Array value long",
                new Long(0).equals(jsonArray.getLong(11)));
        assertTrue("Array value string long",
                new Long(-1).equals(jsonArray.getLong(12)));

        assertTrue("Array value null", jsonArray.isNull(-1));
    }

    //-----------------------------------------------------------------
    //-----------------------------------------------------------------
    //-----------------------------------------------------------------

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

    //-----------------------------------------------------------------
    //-----------------------------------------------------------------
    //-----------------------------------------------------------------

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

    //-----------------------------------------------------------------
    //-----------------------------------------------------------------
    //-----------------------------------------------------------------

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

    //-----------------------------------------------------------------
    //-----------------------------------------------------------------
    //-----------------------------------------------------------------

    /**
     * Sample test case which verifies that no regression in double/BigDecimal support is present.
     */
    @Test
    public void testObjectToBigDecimal() {
        double value = 1412078745.01074;
        Reader reader = new StringReader("[{\"value\": " + value + "}]");
        JSONTokener tokener = new JSONTokener(reader);
        JSONArray array = new JSONArray(tokener);
        JSONObject jsonObject = array.getJSONObject(0);

        BigDecimal current = jsonObject.getBigDecimal("value");
        BigDecimal wantedValue = BigDecimal.valueOf(value);

        assertEquals(current, wantedValue);
    }

    @Test
    public void testObjectToBigDecimal2() {
        Reader reader = new StringReader("[");
        JSONTokener tokener = new JSONTokener(reader);
        try {
            JSONArray array = new JSONArray(tokener);
            fail();
        } catch (JSONException e) {
            assertEquals("Expected a ',' or ']' at 1 [character 2 line 1]", e.getMessage());
        }

    }

    @Test
    public void testObjectToBigDecimal3() {
        Reader reader = new StringReader("[1,2,3");
        JSONTokener tokener = new JSONTokener(reader);
        try {
            JSONArray array = new JSONArray(tokener);
            fail();
        } catch (JSONException e) {
            assertEquals("Expected a ',' or ']' at 6 [character 7 line 1]", e.getMessage());
        }

    }

    @Test
    public void testObjectToBigDecimal4() {
        Reader reader = new StringReader("[1,2,3,");
        JSONTokener tokener = new JSONTokener(reader);
        try {
            JSONArray array = new JSONArray(tokener);
            fail();
        } catch (JSONException e) {
            assertEquals("Expected a ',' or ']' at 7 [character 8 line 1]", e.getMessage());
        }

    }

    //-----------------------------------------------------------------
    //-----------------------------------------------------------------
    //-----------------------------------------------------------------

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

}
