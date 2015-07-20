package edu.berkeley.render.json.marshallers

import grails.converters.JSON
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.codehaus.groovy.grails.web.converters.marshaller.ObjectMarshaller
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.PostConstruct
import groovy.util.logging.Log4j

@Log4j
class DomainJsonMarshaller implements ObjectMarshaller<JSON> {
    @Autowired
    GrailsApplication grailsApplication

    @PostConstruct
    void registerMarshaller() {
        JSON.registerObjectMarshaller(this)
    }

    public boolean supports(Object object) {
        // support if it's a Domain object
        def domain = grailsApplication != null ? grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, object.getClass().name) : null
        log.trace("supports ${object.getClass().getName()} : ${domain != null}")
        return domain != null
    }

    public void marshalObject(Object o, JSON converter) throws ConverterException {
        // Marshal from the properties map of the domain object.  The
        // MapJsonMarshaller will take over if it's registered, which will
        // exclude rendering null values from the properties map.
        converter.config.getMarshaller(o.properties).marshalObject(o.properties, converter)
    }
}
