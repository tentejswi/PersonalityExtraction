/*
 * Copyright (c) 2009-2010, Metaweb Technologies, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *  1. Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *
 *  2. Redistributions in binary form must reproduce the above
 *     copyright notice, this list of conditions and the following
 *     disclaimer in the documentation and/or other materials provided
 *     with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY METAWEB TECHNOLOGIES AND CONTRIBUTORS
 * ``AS IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL METAWEB
 * TECHNOLOGIES OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 */

package com.freebase.json;

import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * This class is a telescopic (meaning, jQuery-like) wrapper for all
 * the various classes that can make up a JSON object.
 * 
 * While using JSON in Javascript or Python comes natural, dealing with it
 * in Java is a pain, mostly because of casting issues. Having a single 
 * wrapper object that encapsulates all those casting issues allows us
 * to cascade the method calls. So something that in javascript would be
 * 
 * <pre>   var a = b.c[1].d[3];</pre>
 *    
 * can be translated in Java as:
 * 
 * <pre>   String a = b.get("c").get(1).get("d").get(3).string();</pre>
 *    
 * which is a lot more verbose but still better than having to 
 * deal with all the casting between Map and List and String by hand.
 */
public class JSON implements JSONAware {

    public enum Type {
        OBJECT, 
        ARRAY, 
        STRING,
        NUMBER,
        BOOLEAN
    }
    
    private Type type;
    
    private JSONObject obj;
    private JSONArray array;
    private String string;
    private Number number;
    private Boolean bool;

    // --------------------------------------------------
    
    public static JSON parse(String s) throws ParseException {
        JSONParser parser = new JSONParser();
        return new JSON(parser.parse(s));
    }

    public static JSON parse(Reader r) throws IOException, ParseException {
        JSONParser parser = new JSONParser();
        return new JSON(parser.parse(r));
    }
    
    // --------------------------------------------------
    
    public JSON(Object o) {
        if (o == null) {
            throw new RuntimeException("can't wrap a null object");
        } else if (o instanceof JSONObject) {
            this.obj = (JSONObject) JSONize((JSONObject) o);
            this.type = Type.OBJECT;
        } else if (o instanceof JSONArray) {
            this.array = (JSONArray) JSONize((JSONArray) o);
            this.type = Type.ARRAY;
        } else if (o instanceof String) {
            this.string = (String) o;
            this.type = Type.STRING;
        } else if (o instanceof Number) {
            this.number = (Number) o;
            this.type = Type.NUMBER;
        } else if (o instanceof Boolean) {
            this.bool = (Boolean) o;
            this.type = Type.BOOLEAN;
        } else {
            throw new RuntimeException("don't how how to deal with this type of object: " + o);
        }
    }
            
    // --------------------------------------------------
    
    public JSON get(String key) {
        if (key == null) throw new RuntimeException("Can't ask for a null key");
        switch (this.type) {
            case BOOLEAN:
            case STRING:
            case NUMBER:
                throw new RuntimeException("Only objects or arrays contain other values");
            case ARRAY:
                int index = Integer.parseInt(key);
                Object ao = this.array.get(index);
                return (JSON) ((ao instanceof JSON || ao == null) ? ao : new JSON(ao));
            case OBJECT:
                Object oo = this.obj.get(key);
                return (JSON) ((oo instanceof JSON || oo == null) ? oo : new JSON(oo));
            default:
                // this should never happen but just in case
                throw new RuntimeException("Don't recognize this object type: " + this.type);
        }
    }

    public JSON get(int index) {
        switch (this.type) {
            case BOOLEAN:
            case STRING:
            case NUMBER:
                throw new RuntimeException("Only objects or arrays contain other values");
            case ARRAY:
                Object ao = this.array.get(index);
                return (JSON) ((ao instanceof JSON || ao == null) ? ao : new JSON(ao));
            case OBJECT:
                Object oo = this.obj.get(Integer.toString(index));
                return (JSON) ((oo instanceof JSON || oo == null) ? oo : new JSON(oo));
            default:
                // this should never happen but just in case
                throw new RuntimeException("Don't recognize this object type: " + this.type);
        }
    }
    
    public boolean has(String key) {
        if (key == null) throw new RuntimeException("Can't ask for a null key");
        switch (this.type) {
            case BOOLEAN:
            case STRING:
            case NUMBER:
                return false;
            case ARRAY:
                int index = Integer.parseInt(key); 
                return (index > 0 && index < this.array.size());
            case OBJECT:
                return this.obj.containsKey(key);
            default:
                // this should never happen but just in case
                throw new RuntimeException("Don't recognize this object type: " + this.type);
        }
    }

    public boolean has(int index) {
        switch (this.type) {
            case BOOLEAN:
            case STRING:
            case NUMBER:
                return false;
            case ARRAY:
                return (index > 0 && index < this.array.size());
            case OBJECT:
                return this.obj.containsKey(Integer.toString(index));
            default:
                // this should never happen but just in case
                throw new RuntimeException("Don't recognize this object type: " + this.type);
        }
    }
        
    public String stringify() {
        return toString();
    }
    
    public String toString() {
        switch (this.type) {
            case BOOLEAN:
                return JSONValue.toJSONString(this.bool);
            case STRING:
                return JSONValue.toJSONString(this.string);
            case NUMBER:
                return JSONValue.toJSONString(this.number);
            case ARRAY:
                return this.array.toJSONString();
            case OBJECT:
                return this.obj.toJSONString();
            default:
                // this should never happen but just in case
                throw new RuntimeException("Don't recognize this object type: " + this.type);
        }
    }

    public String toJSONString() {
        return toString();
    }

    public Object value() {
        switch (this.type) {
            case BOOLEAN:
                return this.bool;
            case STRING:
                return this.string;
            case NUMBER:
                return this.number;
            case ARRAY:
                return this.array;
            case OBJECT:
                return this.obj;
            default:
                // this should never happen but just in case
                throw new RuntimeException("Don't recognize this object type: " + this.type);
        }
    }
    
    public String string() {
        if (this.type != Type.STRING) {
            throw new RuntimeException("This is not a string, it's a " + this.type);
        }
        return this.string;
    }
    
    public Number number() {
        if (this.type != Type.NUMBER) {
            throw new RuntimeException("This is not a number, it's a " + this.type);
        }
        return this.number;
    }
    
    public boolean bool() {
        if (this.type != Type.BOOLEAN) {
            throw new RuntimeException("This is not a boolean, it's a " + this.type);
        }
        return this.bool;
    }
        
    @SuppressWarnings("unchecked")
    public Map object() {
        if (this.type != Type.OBJECT) {
            throw new RuntimeException("This is not an object, it's a " + this.type);
        }
        return this.obj;
    }
    
    @SuppressWarnings("unchecked")
    public List array() {
        if (this.type != Type.ARRAY) {
            throw new RuntimeException("This is not an array, it's a " + this.type);
        }
        return this.array;
    }
    
    public boolean isArray() {
        return (this.type == Type.ARRAY);
    }
    
    public boolean isObject() {
        return (this.type == Type.OBJECT);
    }
    
    public boolean isContainer() {
        return (this.type == Type.OBJECT || this.type == Type.ARRAY);
    }
    
    public Type type() {
        return this.type;
    }
        
    public boolean equals(Object o) {
        if (o instanceof JSON) {
            o = ((JSON) o).value();
        }
        return this.value().equals(o);
    }

    // -----------------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private Object JSONize(Object obj) {
        if (obj instanceof JSONObject) {
            JSONObject o = (JSONObject) obj;
            for (Object key : o.keySet()) {
                Object value = o.get(key);
                if (value instanceof JSONObject || value instanceof JSONArray) {
                    o.put(key,new JSON(value));
                }
            }
        } else if (obj instanceof JSONArray) {
            JSONArray a = (JSONArray) obj;
            for (int i = 0; i < a.size(); i++) {
                Object value = a.get(i);
                if (value instanceof JSONObject || value instanceof JSONArray) {
                    a.set(i, new JSON(value));
                }
            }
        }
        return obj;
    }
    
    // -----------------------------------------------------------------------------------
        
    public static JSON o() {
        return new JSON(new JSONObject());
    }

    @SuppressWarnings("unchecked")
    public static JSON o(Object... objs) {
        JSONObject obj = new JSONObject();
        Object k = null;
        for (Object o : objs) {
            if (k == null) {
                k = o;
            } else {
                if (o != null && !(o instanceof JSON)) {
                    o = new JSON(o);
                }
                obj.put(k.toString(),o);
                k = null;
            }
        }
        if (k != null) {
            throw new RuntimeException("Odd number of arguments, make sure you didn't forget something in your key/value pairs");
        }
        return new JSON(obj);
    }
    
    public static JSON a() {
        return new JSON(new JSONArray());
    }
        
    @SuppressWarnings("unchecked")
    public static JSON a(Object... objs) {
        JSONArray a = new JSONArray();
        for (Object o : objs) {
            if (o != null && !(o instanceof JSON)) {
                o = new JSON(o);
            }
            a.add(o);
        }
        return new JSON(a);
    }
    
    public JSON put(Object k, Object v) {
        return _(k,v);
    }
    
    @SuppressWarnings("unchecked")
    public JSON _(Object k, Object v) {
        if (this.type != Type.OBJECT) {
            throw new RuntimeException("can't add key/value pairs to non-objects types");
        }
        if (v != null && !(v instanceof JSON)) {
            v = new JSON(v);
        }
        this.obj.put(k.toString(), v);
        return this;
    }

    public JSON put(Object o) {
        return _(o);
    }
    
    public JSON add(Object o) {
        return _(o);
    }
    
    @SuppressWarnings("unchecked")
    public JSON _(Object o) {
        if (this.type != Type.ARRAY) {
            throw new RuntimeException("can't add a single value an object type");
        }
        if (!(o instanceof JSON)) {
            o = new JSON(o);
        }
        this.array.add(o);
        return this;
    }
    
    public JSON del(Object o) {
        if (this.type == Type.OBJECT) {
            this.obj.remove(o);
        } else if (this.type == Type.ARRAY) {
            this.array.remove(new JSON(o));
        } else {
            throw new RuntimeException("you can remove stuff only from an object or an array");
        }
        return this;
    }
    
}
