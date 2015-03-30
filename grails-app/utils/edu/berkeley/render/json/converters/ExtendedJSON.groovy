package edu.berkeley.render.json.converters

import grails.converters.JSON
import grails.util.GrailsWebUtil
import groovy.transform.InheritConstructors
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException

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
public class ExtendedJSON extends JSON {
    int startingBufferSize = 8192
    Closure prerenderClosure
    Long lastModified

    /**
     * This is called from
     * org.codehaus.groovy.grails.plugins.converters.api.ConvertersControllersApi
     * void render(controller, Converter converter), which adds "render
     * converterObject" to controllers.
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
}
