package edu.berkeley.render.json.marshallers

import edu.berkeley.util.domain.IncludesExcludesInterface
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.codehaus.groovy.grails.commons.GrailsApplication
import org.codehaus.groovy.grails.web.converters.ConverterUtil
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.codehaus.groovy.grails.web.converters.marshaller.ObjectMarshaller
import org.springframework.beans.factory.annotation.Autowired

import javax.annotation.PostConstruct

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
        String name = ConverterUtil.trimProxySuffix(object.getClass().getName())
        def domain = grailsApplication != null ? grailsApplication.getArtefact(DomainClassArtefactHandler.TYPE, name) : null
        log.trace("when grailsApplication = $grailsApplication, supports ${object.getClass().getName()} : ${domain != null}")
        return domain != null
    }

    public void marshalObject(Object o, JSON converter) throws ConverterException {
        // Marshal from the properties map of the domain object.  The
        // MapJsonMarshaller will take over if it's registered, which will
        // exclude rendering null values from the properties map.
        def marshaller = converter.config.getMarshaller(o.properties)
        log.trace("marshaller for ${o.getClass().name}: ${marshaller.getClass().name}")
        if (marshaller instanceof IncludesExcludesMarshaller) {
            boolean isIncludesExcludes = (o instanceof IncludesExcludesInterface)
            List<String> includes = (isIncludesExcludes && o.includes ? o.includes : null)
            // make a copy of excludes list since we modify it
            List<String> excludes = (isIncludesExcludes && o.excludes ? new ArrayList(o.excludes) : null)

            if (isIncludesExcludes) {
                if (!includes) {
                    log.trace("target may have excludes: ${excludes}")
                    // By default exclude a few fields we know we usually don't want.
                    // If we ever need to override these defaults, the way to do it is probably add parameters to the ConverterConfig annotation, and methods to the interface to control the defaults.
                    List<String> excludeDefaults = getExcludeDefaults()
                    if (!excludes) {
                        // no existing excludes list
                        excludes = excludeDefaults
                    } else {
                        // existing excludes list -- add to it
                        excludes.addAll(excludeDefaults)
                    }
                    log.trace("excludes for converter for ${o.getClass().name}: ${excludes}")
                }
            }
            log.trace("passing to IncludesExcludesMarshaller: includes=$includes, excludes=$excludes for ${o.getClass().name}, isIncludesExcludes=$isIncludesExcludes")
            marshaller.marshalObject(o.properties, converter, includes, excludes)
        } else {
            marshaller.marshalObject(o.properties, converter)
        }
    }

    protected List<String> getExcludeDefaults() {
        return ["class", "excludes", "includes"]
    }
}
