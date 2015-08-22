package io.codearte.accurest.util

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import groovy.json.JsonSlurper
import spock.lang.Specification

class JsonConverterSpec extends Specification {

	//TODO: Should not verify via concrete element in the list (LENIENT verification should be on)
	def 'should convert a json with list as root to a map of path to value'() {
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
			Map<String, Object> pathAndValues = JsonConverter.transformToJsonPathWithValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues['$[0].some.nested.json'] == 'with value'
			pathAndValues['$[0].some.nested.anothervalue'] == 4
			pathAndValues['$[0].some.nested.withlist[0]'] == 'a'
			pathAndValues['$[0].some.nested.withlist[1]'] == 'b'
			pathAndValues['$[0].some.nested.withlist[2]'] == 'c'
		and:
			assertThatJsonPathsInMapAreValid(json, pathAndValues)

		}

	def 'should convert a json with a map as tooy to a map of path to value'() {
		given:
			String json = '''
					 {
						"some" : {
							"nested" : {
								"json" : "with value",
								"anothervalue": 4,
								"withlist" : [
									"a" , "b", "c"
								]
							}
						}
					}
'''
		when:
			Map<String, Object> pathAndValues = JsonConverter.transformToJsonPathWithValues(new JsonSlurper().parseText(json))
		then:
			pathAndValues['$.some.nested.json'] == 'with value'
			pathAndValues['$.some.nested.anothervalue'] == 4
			pathAndValues['$.some.nested.withlist[0]'] == 'a'
			pathAndValues['$.some.nested.withlist[1]'] == 'b'
			pathAndValues['$.some.nested.withlist[2]'] == 'c'
		and:
			assertThatJsonPathsInMapAreValid(json, pathAndValues)

		}

	private void assertThatJsonPathsInMapAreValid(String json, Map<String, Object> pathAndValues) {
		Object parsedJson = Configuration.defaultConfiguration().jsonProvider().parse(json);
		pathAndValues.entrySet().each {
			assert JsonPath.read(parsedJson, it.key) == it.value
		}
	}
}
