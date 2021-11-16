package org.json.junit;

import org.json.*;
import org.json.junit.data.Fraction;
import org.json.junit.data.MyNumber;
import org.json.junit.data.MyNumberContainer;
import org.json.junit.data.SingletonEnum;
import org.junit.Test;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class TestExperimentSet2 {

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

}
