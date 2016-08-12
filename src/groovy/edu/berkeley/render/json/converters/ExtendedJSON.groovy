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

package edu.berkeley.render.json.converters

import edu.berkeley.render.json.JSONString
import edu.berkeley.render.json.marshallers.DomainJsonMarshaller
import edu.berkeley.render.json.marshallers.MapJsonMarshaller
import grails.converters.JSON
import grails.util.GrailsWebUtil
import groovy.transform.InheritConstructors
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.codehaus.groovy.grails.web.json.JSONArray
import org.codehaus.groovy.grails.web.json.JSONObject

import javax.servlet.http.HttpServletResponse
import java.nio.charset.Charset

/**
 * This extends the standard JSON converter and adds a Content-Length header
 * when rendering to a HttpServletResponse, and also adds the ability to add
 * a Last-Modified header.  In addition, a prerender closure may be called
 * just before rendering the response.
 *
 * Examples of using this in a controller:
 * render ((person as ExtendedJSON).setLastModified(person.timeUpdated))
 * render ((person as ExtendedJSON).prerender() {println("process here")})
 */
@InheritConstructors
@Log4j
public class ExtendedJSON extends JSON {
    int startingBufferSize = 8192
    Closure prerenderClosure
    Long lastModified

    /**
     * This is called from
     * org.codehaus.groovy.grails.plugins.converters.api.ConvertersControllersApi
     * void render(controller, Converter converter), which adds "render
     * converterObject" to controllers.
     *
     * When you do <code>(foo as ExtendedJSON).toString()</code>, this is
     * equivalent to <code>new ExtendedJSON(foo).toString()</code>.
     *
     * When you do <code>render foo as ExtendedJSON</code> in a controller,
     * this is equivalent to
     * <code>ConvertersControllersApi.render(thisController, new ExtendedJSON(foo))</code>,
     * which does <code>fooConverter.render(controller.response)</code>.
     *
     * Use the @ConverterConfig annotation (from the grails-domain-util
     * library) on the target object if you want to control which fields get
     * marshalled.
     */
    @Override
    public void render(HttpServletResponse response) throws ConverterException {
        response.setContentType(GrailsWebUtil.getContentType(contentType, encoding))
        try {
            StringWriter writer = new StringWriter(startingBufferSize)
            // render the json to the string buffer
            render(writer)
            String responseString = writer.getBuffer()
            // set Content-Length header based on what's in the buffer
            response.setContentLength(responseString.getBytes(Charset.forName("UTF-8")).length)
            // set Last-Modified header if it was specified
            if (lastModified != null)
                response.setDateHeader("Last-Modified", lastModified)
            // call the prerender closure if it was specified
            if (prerenderClosure) {
                prerenderClosure(responseString)
            }
            // render the response to the HttpServletResponse
            response.getWriter().write(responseString)
        }
        catch (IOException e) {
            throw new ConverterException(e)
        }
    }

    /**
     * Set a closure to be called just before response rendering.
     */
    ExtendedJSON prerender(Closure closure) {
        this.prerenderClosure = closure
        return this
    }

    /**
     * Set the time for the Last-Modified header.
     */
    ExtendedJSON setLastModified(Long timestamp) {
        this.lastModified = timestamp
        return this
    }

    /**
     * Set the time for the Last-Modified header.
     */
    ExtendedJSON setLastModified(Date date) {
        this.lastModified = (date ? date.time : null)
        return this
    }

    static Closure getDoWithSpringRegisterMarshallersClosure() {
        return {
            // Register a custom marshaller for JSONString instances
            jsonStringMarshaller(JSONString.ToStringJsonMarshaller)

            // Register a custom marshaller for Domain class instances that will
            // marshal a domain instance as a Map, at which point the
            // MapJsonMarshaller takes over.
            domainMarshaller(DomainJsonMarshaller)

            // Register a custom marshaller for Map instances that will exclude
            // rendering entries with null values
            mapMarshaller(MapJsonMarshaller)
        }
    }

    JSONObject toJSONObject() {
        return (JSONObject) parse(this.toString())
    }

    JSONArray toJSONArray() {
        return (JSONArray) parse(this.toString())
    }

    Map toMap() {
        return toJSONObject()
    }

    List toList() {
        return toJSONArray()
    }
}
