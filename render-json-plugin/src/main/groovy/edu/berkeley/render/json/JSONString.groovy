/*
 * Copyright (c) 2016, Regents of the University of California and
 * contributors.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS
 * IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package edu.berkeley.render.json

import grails.converters.JSON
import org.grails.web.converters.exceptions.ConverterException
import org.grails.web.converters.marshaller.ObjectMarshaller

import javax.annotation.PostConstruct

/**
 * Wraps a regular string to identify it as a JSON string.  Then we can,
 * using Spring, register a custom marshaller for JSONStrings that doesn't
 * try to re-marshal this type of object.
 */
class JSONString implements Writable, Serializable {
    static class ToStringJsonMarshaller implements ObjectMarshaller<JSON> {
        boolean supports(Object object) {
            return object instanceof JSONString
        }

        void marshalObject(Object object, JSON converter)
                throws ConverterException {
            // Converter.getWriter() is a JSONWriter object.
            // Need the ability to directly write non-quoted strings, so add
            // a method to do that.
            if (!converter.getWriter().metaClass.pickMethod("directAppend", [String] as Class[])) {
                converter.getWriter().metaClass.directAppend = { String val ->
                    return val != null ? append(val) : valueNull()
                }
            }

            converter.getWriter().directAppend(object.toString())
        }

        @PostConstruct
        void registerMarshaller() {
            JSON.registerObjectMarshaller(this)
        }
    }

    private String json

    public JSONString(String json) {
        this.json = json;
    }

    public String getJson() {
        return json
    }

    @Override
    public String toString() {
        return json;
    }

    @Override
    public int hashCode() {
        return json.hashCode()
    }

    @Override
    public boolean equals(Object o) {
        return o == json
    }

    public Writer writeTo(Writer out) throws IOException {
        out.write(json)
        return out
    }
}
