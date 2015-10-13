package io.codearte.accurest.dsl.internal

import groovy.transform.CompileStatic

import java.util.regex.Pattern

@CompileStatic
class PatternProperty {

	final Pattern value

	PatternProperty(String regex) {
		this.value = ~/$regex/
	}

	PatternProperty(Pattern value) {
		this.value = value
	}

	String pattern() {
		return value.pattern()
	}

	@Override
	String toString() {
		return value
	}
}
