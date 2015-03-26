package edu.berkeley.render.json.marshallers

import grails.converters.JSON
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import org.codehaus.groovy.grails.web.converters.marshaller.json.MapMarshaller

import javax.annotation.PostConstruct

class MapJsonMarshaller extends MapMarshaller {
    @PostConstruct
    void registerMarshaller() {
        JSON.registerObjectMarshaller(this);
    }

    @Override
    public void marshalObject(Object o, JSON converter) throws ConverterException {
        // remove entries with null values
        super.marshalObject(o.findAll { it.value != null }, converter)
    }
}
