package io.codearte.accurest.dsl.internal

import java.util.regex.Pattern

class PatternProperty {

	final Pattern value

	PatternProperty(String regex) {
		this.value = Pattern.compile(regex)
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
