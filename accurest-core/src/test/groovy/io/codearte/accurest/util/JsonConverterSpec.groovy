package io.codearte.accurest.util

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import groovy.json.JsonSlurper
import spock.lang.Specification

class JsonConverterSpec extends Specification {

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
			Object parsedJson = Configuration.defaultConfiguration().jsonProvider().parse(json);
			pathAndValues.entrySet().each {
				assert JsonPath.read(parsedJson, it.key) == it.value
			}
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
			Object parsedJson = Configuration.defaultConfiguration().jsonProvider().parse(json);
			pathAndValues.entrySet().each {
				assert JsonPath.read(parsedJson, it.key) == it.value
			}
		}
}
