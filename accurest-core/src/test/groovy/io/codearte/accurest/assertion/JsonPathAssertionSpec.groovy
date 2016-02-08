package io.codearte.accurest.assertion

import groovy.json.JsonSlurper
import org.junit.Ignore
import spock.lang.Specification

import static io.codearte.accurest.assertion.JsonPathAssertion.assertThat
/**
 * @author Marcin Grzejszczak
 */
public class JsonPathAssertionSpec extends Specification {

	def 'should work'() {
		expect:
			assertThat('body').field('foo').isEqualTo('bar')
	}

	@Ignore
	def 'should convert a json with a map as root to a map of path to value'() {
		given:
		String json = '''
					 {
							"some" : {
								"nested" : {
									"json" : "with value",
									"anothervalue": 4,
									"withlist" : [
										{ "name" :"name1"} , {"name": "name2"}
									]
								}
							}
						}
'''
		when:
		JsonPathsAssertions pathAndValues = JsonToJsonPathsAssertionsConverter.transformToJsonPathWithTestsSideValues(new JsonSlurper().parseText(json))
		then:
		pathAndValues['''$.some.nested[?(@.json == 'with value')]'''] == 'with value'
		pathAndValues['''assertThat('body').field('some')..array('nested').contains('anothervalue').isEqualTo(4)''']
		pathAndValues['''$.some.nested[?(@.anothervalue == 4)]'''] == 4
		pathAndValues['''$.some.nested.withlist[*][?(@.name == 'name1')]'''] == 'name1'
		pathAndValues['''$.some.nested.withlist[*][?(@.name == 'name2')]'''] == 'name2'

	}

}