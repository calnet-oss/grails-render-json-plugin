package edu.berkeley.render.json.test

import edu.berkeley.util.domain.transform.ConverterConfig

// exclude the dummyField when marshalling to JSON
@ConverterConfig(excludes = ["dummyField"])
class TestPerson extends AbstractTestPerson {
}
