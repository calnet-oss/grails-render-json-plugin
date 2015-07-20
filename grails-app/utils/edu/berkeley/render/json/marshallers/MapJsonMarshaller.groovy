package edu.berkeley.render.json.marshallers

import edu.berkeley.render.json.converters.ExtendedJSON
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.support.IncludeExcludeSupport
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.codehaus.groovy.grails.web.converters.marshaller.json.MapMarshaller

import javax.annotation.PostConstruct

@Log4j
class MapJsonMarshaller extends MapMarshaller {
    @PostConstruct
    void registerMarshaller() {
        JSON.registerObjectMarshaller(this);
    }

    @Override
    public void marshalObject(Object o, JSON converter) throws ConverterException {

        boolean isExtendedJSON = converter instanceof ExtendedJSON

        Class<?> clazz = o.getClass()
        List<String> excludes = (isExtendedJSON ? converter.getExcludes() : converter.getExcludes(clazz))
        List<String> includes = (isExtendedJSON ? converter.getIncludes() : converter.getIncludes(clazz))
        IncludeExcludeSupport<String> includeExcludeSupport = new IncludeExcludeSupport<String>()

        Map map = (Map) o
        // if not already so, convert to a SortedMap so we render json map keys in the same order
        if (!(map instanceof SortedMap)) {
            map = new TreeMap(map)
        }

        // remove entries with null values
        // also check includes/excludes
        super.marshalObject(
                map.findAll {
                    it.value != null && shouldInclude(includeExcludeSupport, includes, excludes, o, it.key)
                    log.trace("${it.key}: value=${it.value}, shouldInclude=${shouldInclude(includeExcludeSupport, includes, excludes, o, it.key)}")
                },
                converter
        )
    }

    // from org.codehaus.groovy.grails.web.converters.marshaller.json.DomainClassMarshaller
    protected boolean shouldInclude(
            IncludeExcludeSupport<String> includeExcludeSupport,
            List<String> includes,
            List<String> excludes,
            Object object,
            String propertyName
    ) {
        return includeExcludeSupport.shouldInclude(includes, excludes, propertyName) && shouldInclude(object, propertyName)
    }

    // from org.codehaus.groovy.grails.web.converters.marshaller.IncludeExcludePropertyMarshaller
    protected boolean shouldInclude(Object object, String propertyName) {
        return includesProperty(object, propertyName) && !excludesProperty(object, propertyName)
    }

    // from org.codehaus.groovy.grails.web.converters.marshaller.IncludeExcludePropertyMarshaller
    // Override for custom exclude logic
    protected boolean excludesProperty(Object object, String property) {
        return false
    }

    // from org.codehaus.groovy.grails.web.converters.marshaller.IncludeExcludePropertyMarshaller
    // Override for custom include logic
    protected boolean includesProperty(Object object, String property) {
        return true
    }
}
