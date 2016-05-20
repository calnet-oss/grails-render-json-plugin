package edu.berkeley.render.json.marshallers

import grails.converters.JSON

interface IncludesExcludesMarshaller {
    void marshalObject(Object obj, JSON converter, List<String> includes, List<String> excludes, Boolean includeNulls)
}
