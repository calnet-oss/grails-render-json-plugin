package edu.berkeley.render.json.test

import edu.berkeley.util.domain.transform.ConverterConfig

// include everything but the dummyField
@ConverterConfig(includes = ["uid", "emailAddress", "dateOfBirth", "timeCreated", "timeUpdated", "firstName", "lastName"])
class TestPersonIncludesAnnotation extends AbstractTestPerson {
}
