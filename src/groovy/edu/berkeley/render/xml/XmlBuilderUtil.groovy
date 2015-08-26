package edu.berkeley.render.xml

import groovy.xml.MarkupBuilder

class XmlBuilderUtil {
    /**
     * Convert a map to a XML string.
     *
     * @param rootElementName The name of the root element in the XML string.
     * @param map The map to convert to XML.
     *
     * @return XML as a String.
     */
    static String mapToXmlString(String rootElementName, Map map) {
        StringWriter sw = new StringWriter()
        new MarkupBuilder(sw).with {
            def visitor
            visitor = { k, v ->
                "$k" { v instanceof Map ? v.collect(visitor) : mkp.yield(v) }
            }
            "$rootElementName" { map.collect(visitor) }
        }
        return sw as String
    }

    /**
     * Convert a map to a XML string.
     *
     * @param map The map to convert.  This map must be one entry, with the
     *        key being a String and the value being another Map.  The root
     *        element name will be the key used in this single-entry
     *        passed-in map.  Use mapToXmlString(String rootElementName, Map
     *        map) instead if your map has more than one entry at the top
     *        level.
     *
     * @return XML as a String.
     */
    static String mapToXmlString(Map map) {
        if (map.size() == 1 && map.values()[0] instanceof Map) {
            Map.Entry<String, Map> onlyEntry = map.entrySet()[0]
            return mapToXmlString(onlyEntry.key, onlyEntry.value)
        } else {
            throw new IllegalArgumentException("The passed in map has more than one entry so unable to automatically determine the root element name.  Use mapToXmlString(String rootElementName, Map map) instead.")
        }
    }
}
