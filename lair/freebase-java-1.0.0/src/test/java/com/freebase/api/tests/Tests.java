package com.freebase.api.tests;

import static com.freebase.json.JSON.o;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.freebase.api.Freebase;
import com.freebase.json.JSON;

public class Tests {
        
    @Before public void setup() {
        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
        System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
        System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.wire", "debug");
    }
        
    @Test public void testMQLRead() {
        Freebase freebase = Freebase.getFreebase();
        JSON query = o("id",null,"limit",1);
        JSON response = freebase.mqlread(query);
        assertTrue("/user/root".equals(response.get("result").get("id").string()));
    }

    @Test public void testSearch() {
        Freebase freebase = Freebase.getFreebase();
        JSON result = freebase.search("blade runner");
        String id = result.get("result").get(0).get("id").string();
        assertTrue(id.equals("/en/blade_runner"));
    }

    @Test public void testGetTopic() {
        Freebase freebase = Freebase.getFreebase();
        JSON topic = freebase.get_topic("/en/blade_runner",o("mode","basic"));
        String name = topic.get("result").get("text").string();
        assertTrue(name.equals("Blade Runner"));
    }

    @Test public void testMQLWrite() {
        Freebase sandbox = Freebase.getFreebaseSandbox();
        sandbox.sign_in("stefanomazzocchi2", "stefano");

        String name = "test" + System.currentTimeMillis();
        JSON query = o()
                       ._("create","unless_exists")
                       ._("name",name)
                       ._("type","/common/topic")
                       ._("id",null);

        String id = sandbox.mqlwrite(query).get("result").get("id").string();
        String name2 = sandbox.mqlread(JSON.o("id",id,"name",null)).get("result").get("name").string();
        assertTrue(name.equals(name2));
    }
    
    @Test public void testUpload() {
        Freebase sandbox = Freebase.getFreebaseSandbox();
        sandbox.sign_in("stefanomazzocchi2", "stefano");
        String content = "blah blah";
        String media_type = "text/plain";
        JSON result = sandbox.upload(content, media_type);
        String id = result.get("result").get("id").string();
        String retrieved_content = sandbox.get_blob(id);
        assertTrue(content.equals(retrieved_content));
        String retrieved_media_type = result.get("result").get("/type/content/media_type").string();
        assertTrue(media_type.equals(retrieved_media_type));
    }
}

