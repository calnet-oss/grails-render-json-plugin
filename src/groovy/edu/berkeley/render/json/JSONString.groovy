package edu.berkeley.render.json

import grails.converters.JSON
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.codehaus.groovy.grails.web.converters.marshaller.ObjectMarshaller

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
