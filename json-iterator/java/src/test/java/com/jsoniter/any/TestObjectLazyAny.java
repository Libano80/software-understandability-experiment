package com.jsoniter.any;

import com.jsoniter.JsonIterator;
import junit.framework.TestCase;

public class TestObjectLazyAny extends TestCase {

    public void test_fillCacheUntil_ObjectLazyAny() {
        Any obj = JsonIterator.deserialize("{\"field1\":\"field1\"}");
        assertTrue(obj instanceof ObjectLazyAny);
        ObjectLazyAny objectLazyAny = (ObjectLazyAny) obj;
        Any any = objectLazyAny.fillCacheUntil("field1");
        assertEquals("field1", any.toString());
    }

    public void test_fillCacheUntil_ObjectLazyAny2() {
        Any obj = JsonIterator.deserialize("{\"field1\":\"field1\", \"field2\":\"field2\"}");
        assertTrue(obj instanceof ObjectLazyAny);
        ObjectLazyAny objectLazyAny = (ObjectLazyAny) obj;
        Any field1 = objectLazyAny.fillCacheUntil("field1");
        assertEquals("field1", field1.toString());
        Any field2 = objectLazyAny.fillCacheUntil("field2");
        assertEquals("field2", field2.toString());
    }

    public void test_fillCacheUntil_ObjectLazyAny3() {
        Any obj = JsonIterator.deserialize("{\"field1\":\"field1\", \"field2\":\"field2\"}");
        assertTrue(obj instanceof ObjectLazyAny);
        ObjectLazyAny objectLazyAny = (ObjectLazyAny) obj;
        Any field2 = objectLazyAny.fillCacheUntil("field2");
        assertEquals("field2", field2.toString());
        Any field1 = objectLazyAny.fillCacheUntil("field1");
        assertEquals("field1", field1.toString());
    }

    public void test_fillCacheUntil_ObjectLazyAny4() {
        Any obj = JsonIterator.deserialize("{\"field1\":\"hello\", \"field2\":\"world\"}");
        assertTrue(obj instanceof ObjectLazyAny);
        ObjectLazyAny objectLazyAny = (ObjectLazyAny) obj;
        Any field2 = objectLazyAny.fillCacheUntil("field2");
        assertEquals("world", field2.toString());
        Any field1 = objectLazyAny.fillCacheUntil("field1");
        assertEquals("hello", field1.toString());
    }

    public void test_fillCacheUntil_ObjectLazyAny5() {
        Any obj = JsonIterator.deserialize("{\"field1\":1, \"field2\":2}");
        assertTrue(obj instanceof ObjectLazyAny);
        ObjectLazyAny objectLazyAny = (ObjectLazyAny) obj;
        Any field1 = objectLazyAny.fillCacheUntil("field1");
        assertEquals(1, field1.toInt());
        Any field2 = objectLazyAny.fillCacheUntil("field2");
        assertEquals(2, field2.toInt());
    }

}
