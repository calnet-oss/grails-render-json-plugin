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

package edu.berkeley.render.json.marshallers

import edu.berkeley.util.domain.IncludesExcludesInterface
import grails.converters.JSON
import groovy.util.logging.Log4j
import org.grails.core.util.IncludeExcludeSupport
import org.grails.web.converters.exceptions.ConverterException
import org.grails.web.converters.marshaller.json.MapMarshaller

import javax.annotation.PostConstruct

@Log4j
class MapJsonMarshaller extends MapMarshaller implements IncludesExcludesMarshaller {
    @PostConstruct
    void registerMarshaller() {
        JSON.registerObjectMarshaller(this);
    }

    public void marshalObject(Object obj, JSON converter, List<String> includes, List<String> excludes, Boolean includeNulls) throws ConverterException {
        Map map = (Map) obj
        // if not already so, convert to a SortedMap so we render json map
        // keys in the same order
        if (!(map instanceof SortedMap)) {
            map = new TreeMap(map)
        }

        IncludeExcludeSupport<String> includeExcludeSupport = new IncludeExcludeSupport<String>()

        // remove entries with null values
        // also check includes/excludes
        def toMarshal = map.findAll {
            log.trace("${it.key}: value=${it.value}, includeNulls=$includeNulls, shouldInclude=${shouldInclude(includeExcludeSupport, includes, excludes, map, it.key)}")
            (includeNulls || it.value != null) && shouldInclude(includeExcludeSupport, includes, excludes, map, it.key)
        }
        log.trace("Marshalling the following: $toMarshal")
        super.marshalObject(toMarshal, converter)
    }

    @Override
    public void marshalObject(Object obj, JSON converter) throws ConverterException {
        // while typically it's the domain class that implements the
        // IncludesExcludesInterface, the map itself may implement it
        boolean isIncludesExcludes = (obj instanceof IncludesExcludesInterface)
        if (isIncludesExcludes) {
            marshalObject(obj, converter, obj.getIncludes(), obj.getExcludes(), obj.getIncludeNulls())
        } else {
            marshalObject(obj, converter, null, null, false)
        }
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
