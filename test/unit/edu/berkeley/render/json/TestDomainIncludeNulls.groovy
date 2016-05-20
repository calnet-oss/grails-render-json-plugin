package edu.berkeley.render.json

import edu.berkeley.util.domain.IncludesExcludesInterface

class TestDomainIncludeNulls implements IncludesExcludesInterface {
    List<String> includes
    List<String> excludes
    Boolean includeNulls = true

    Map helloMap = ["helloArray": ["world1", "world2"]]

    Object shouldBeNull
}
