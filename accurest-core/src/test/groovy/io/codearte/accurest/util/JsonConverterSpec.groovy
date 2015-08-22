package io.codearte.accurest.util

import groovy.json.JsonSlurper
import spock.lang.Specification

class JsonConverterSpec extends Specification {

	def 'should convert a json to a map of path to value'() {
		given:
		String json = '''
					[ {
							"some" : {
								"nested" : {
									"json" : "with value",
									"anothervalue": 4,
									"withlist" : [
										"a" , "b", "c"
									]
								}
							}
						},
						{
							"someother" : {
								"nested" : {
									"json" : "with value",
									"anothervalue": 4,
									"withlist" : [
										"a" , "b", "c"
									]
								}
							}
						}
					]
'''
		when:
			Map<String, Object> pathAndValues = JsonConverter.transformToPathAndValues(new JsonSlurper().parseText(json))

		then:
			pathAndValues['$.some.nested.json'] == 'with value'
			pathAndValues['$.some.nested.anothervalue'] == 4
			pathAndValues['$.some.nested.withlist[0]'] == 'a'
			pathAndValues['$.some.nested.withlist[1]'] == 'b'
			pathAndValues['$.some.nested.withlist[2]'] == 'c'
		}
}
