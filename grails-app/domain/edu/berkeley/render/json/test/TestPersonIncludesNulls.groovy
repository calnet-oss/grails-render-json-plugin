package edu.berkeley.render.json.test

import edu.berkeley.util.domain.transform.ConverterConfig

// exclude the dummyField when marshalling to JSON and include null fields in the output
@ConverterConfig(excludes = ["dummyField"], includeNulls = true)
class TestPersonIncludesNulls extends AbstractTestPerson {
}
