package com.freebase.json.tests;

import static com.freebase.json.JSON.a;
import static com.freebase.json.JSON.o;
import static org.junit.Assert.assertTrue;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.junit.Test;

import com.freebase.json.JSON;

public class Tests {

    String json1 = "{ 'id' : null , 'limit' : 1 }".replace('\'', '"');
    String json2 = "[{ 'a' : 1 , 'b' : true , 'c' : null , 'd' : { 'a1' : [ 'blah' ] }}]".replace('\'', '"');
    String json3 = "{ 'a' : 'b' , 'c' : [ 'd' , 'f' ]}".replace('\'', '"');
    String json4 = "{ 'c' : [ 'f' ]}".replace('\'', '"');
    String json5 = "[ 1 , null, { 'blah' : 'blah' }]".replace('\'', '"');
    
    @Test public void test_JSON_parser() throws ParseException {
        JSONParser parser = new JSONParser();
        Object json = parser.parse(json1);
        assertTrue(json instanceof JSONObject);
    }
    
    @Test public void test_JSON_constructors_1() throws ParseException {
        // make the json object by using the o/a notation
        JSON j1 = o("id",null,"limit",1);

        // make the json object parsing the javascript syntax from a string 
        JSON j2 = JSON.parse(json1);

        // make sure they serialize the same
        assertTrue(j1.toString().equals(j2.toString()));
    }
    
    @Test public void test_JSON_constructors_2() throws ParseException {
        // make the json object by using the o/a notation
        JSON j1 = a(o("a",1,"b",true,"c",null,"d",o("a1",a("blah"))));
        
        // make the json object parsing the javascript syntax from a string 
        JSON j2 = JSON.parse(json2);
        
        // make sure they serialize the same
        assertTrue(j1.toString().equals(j2.toString()));
    }

    @Test public void test_JSON_augmenter_1() throws ParseException {
        // make the json object by using the o/a notation
        JSON j1 = a()._(o()._("a",1)._("b",true)._("c",null)._("d",o()._("a1",a()._("blah"))));

        // make the json object parsing the javascript syntax from a string 
        JSON j2 = JSON.parse(json2);

        // make sure they serialize the same
        assertTrue(j1.toString().equals(j2.toString()));
    }
    
    @Test public void test_JSON_remover_1() throws ParseException {
        // make the json object parsing the javascript syntax from a string 
        JSON j1 = JSON.parse(json3);
        
        // modify the object
        j1.del("a").get("c").del("d");

        // make the json object parsing the javascript syntax from a string 
        JSON j2 = JSON.parse(json4);
                
        // make sure they serialize the same
        assertTrue(j1.toString().equals(j2.toString()));
    }
    
    @Test public void test_JSON_remover_2() throws ParseException {
        // make the json object parsing the javascript syntax from a string 
        JSON j1 = a(1,null,"b",o("blah","blah"));
        
        // modify the object
        j1.del("b");

        // make the json object parsing the javascript syntax from a string 
        JSON j2 = JSON.parse(json5);
                
        // make sure they serialize the same
        assertTrue(j1.toString().equals(j2.toString()));
    }
    
}

