package com.jsoniter.any;

import com.jsoniter.IterImplForStreaming;
import com.jsoniter.JsonIterator;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class TestArray extends TestCase {
    public void test_size() {
        Any any = Any.wrap(new int[]{1, 2, 3});
        assertEquals(3, any.size());
    }

    public void test_to_boolean() {
        Any any = Any.wrap(new int[0]);
        assertFalse(any.toBoolean());
        any = Any.wrap(new Object[]{"hello", 1});
        assertTrue(any.toBoolean());
    }

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

    public void test_to_int() {
        Any any = Any.wrap(new int[0]);
        assertEquals(0, any.toInt());
        any = Any.wrap(new Object[]{"hello", 1});
        assertEquals(2, any.toInt());
    }

    public void test_get() {
        Any any = Any.wrap(new Object[]{"hello", 1});
        assertEquals("hello", any.get(0).toString());
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

    public void test_get_from_nested() {
        Any any = Any.wrap(new Object[]{new String[]{"hello"}, new String[]{"world"}});
        assertEquals("hello", any.get(0, 0).toString());
        assertEquals("[\"hello\",\"world\"]", any.get('*', 0).toString());
    }

    public void test_iterator() {
        Any any = Any.wrap(new long[]{1, 2, 3});
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (Any element : any) {
            list.add(element.toInt());
        }
        assertEquals(Arrays.asList(1, 2, 3), list);
    }

    public void test_to_string() {
        assertEquals("[1,2,3]", Any.wrap(new long[]{1, 2, 3}).toString());
        Any any = Any.wrap(new long[]{1, 2, 3});
        any.asList().add(Any.wrap(4));
        assertEquals("[1,2,3,4]", any.toString());
    }

    public void test_fill_partial_then_iterate() {
        Any obj = JsonIterator.deserialize("[1,2,3]");
        assertEquals(1, obj.get(0).toInt());
        Iterator<Any> iter = obj.iterator();
        assertEquals(1, iter.next().toInt());
        assertEquals(2, iter.next().toInt());
        assertEquals(3, iter.next().toInt());
        assertFalse(iter.hasNext());
    }

    public void test_equals_and_hashcode() {
        Any obj1 = JsonIterator.deserialize("[1,2,3]");
        Any obj2 = JsonIterator.deserialize("[1, 2, 3]");
        assertEquals(obj1, obj2);
        assertEquals(obj1.hashCode(), obj2.hashCode());
    }

    public void test_null() {
        Any x = JsonIterator.deserialize("{\"test\":null}");
        assertFalse(x.get("test").iterator().hasNext());
    }

    public void test_get_ObjectLazyAny() {
        Any any = JsonIterator.deserialize("{\"test\":null}");
        assertEquals("null", any.get("test").toString());
    }

    public void test_get_ObjectLazyAny2() {
        Any any = JsonIterator.deserialize("{\"test\":\"value\"}");
        assertEquals("value", any.get("test").toString());
    }

    public void test_get_ObjectLazyAny3() {
        Any any = JsonIterator.deserialize("{\"test\":\"value\"}");
        assertEquals("value", any.get("test").toString());
    }

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
