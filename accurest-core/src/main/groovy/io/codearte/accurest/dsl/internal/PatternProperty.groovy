package io.codearte.accurest.dsl.internal

import java.util.regex.Pattern

class PatternProperty {
	final Pattern value

	PatternProperty(Pattern value) {
		this.value = value
	}

	String pattern() {
		return value.pattern()
	}

}
