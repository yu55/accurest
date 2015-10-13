package io.codearte.accurest.dsl.internal

import groovy.transform.CompileStatic

@CompileStatic
class OptionalProperty extends PatternProperty {

	OptionalProperty(Object value) {
		super(~/($value)?/)
	}
}
