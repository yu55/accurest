package io.codearte.accurest.plugin

import spock.lang.Stepwise

@Stepwise
class SampleJerseyProjectSpec extends AccurestIntegrationSpec {

	void setup() {
		copyResources("functionalTest/sampleJerseyProject", "")
		runTasksSuccessfully('clean')
		//delete accidental output when previously importing SimpleBoot into Idea to tweak it
	}

	def "should pass basic flow for Spock"() {
		given:
			assert fileExists('build.gradle')
		expect:
			runTasksSuccessfully('check')
	}

	def "should pass basic flow for JUnit"() {
		given:
			switchToJunitTestFramework()
			assert fileExists('build.gradle')
		expect:
			runTasksSuccessfully('check')
	}

}
