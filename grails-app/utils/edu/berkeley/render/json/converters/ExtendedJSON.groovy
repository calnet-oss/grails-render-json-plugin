package edu.berkeley.render.json.converters

import edu.berkeley.util.domain.IncludesExcludesInterface
import grails.converters.JSON
import grails.util.GrailsWebUtil
import groovy.transform.InheritConstructors
import groovy.util.logging.Log4j
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
@Log4j
public class ExtendedJSON extends JSON {
    int startingBufferSize = 8192
    Closure prerenderClosure
    Long lastModified

    List<String> getIncludes() {
        return getIncludes(target.getClass())
    }

    List<String> getExcludes() {
        return getExcludes(target.getClass())
    }

    protected List<String> getExcludeDefaults() {
        return ["class", "excludes", "includes"]
    }

    @Override
    public void setTarget(Object target) {
        boolean targetAlreadySet = target
        super.setTarget(target)
        /**
         * If the target is an instance of IncludesExcludesInterface (such
         * as from the @ConverterConfig annotation), then we can use the
         * getIncludes() and getExcludes() method to configure the
         * converter's includes and excludes.
         */
        log.trace("${target.getClass().name} instanceof IncludesExcludesInterface? : ${target instanceof IncludesExcludesInterface}")
        if (target instanceof IncludesExcludesInterface) {
            // only reset excludes and includes if this is not the first call to setTarget()
            if (targetAlreadySet) {
                log.trace("clearing existing excludes and includes because setTarget() called twice")
                setExcludes(null)
                setIncludes(null)
            }
            if (target.includes) {
                log.trace("target has includes: ${target.includes}")
                if (!getIncludes()) {
                    // no existing includes list -- use from target
                    setIncludes(target.includes)
                } else {
                    // existing includes list -- add to it
                    getIncludes().addAll(target.includes)
                }
                log.trace("includes for converter for ${target.getClass().name}: ${getIncludes()}")
            } else {
                log.trace("target may have excludes: ${target.excludes}")
                // By default exclude a few fields we know we usually don't want.
                // If we ever need to override these defaults, the way to do it is probably add parameters to the ConverterConfig annotation, and methods to the interface to control the defaults.
                List<String> excludeDefaults = getExcludeDefaults()
                if (!getExcludes()) {
                    // no existing excludes list
                    setExcludes(excludeDefaults)
                } else {
                    // existing excludes list -- add to it
                    getExcludes().addAll(excludeDefaults)
                }
                // add the target's excludes
                getExcludes().addAll(target.excludes)
                log.trace("excludes for converter for ${target.getClass().name}: ${getExcludes()}")
            }
        }
    }

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
}
