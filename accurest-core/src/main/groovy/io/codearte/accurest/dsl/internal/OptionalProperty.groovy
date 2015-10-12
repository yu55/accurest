package io.codearte.accurest.dsl.internal

import java.util.regex.Pattern

class OptionalProperty extends PatternProperty {

	OptionalProperty(Object value) {
		super(Pattern.compile("($value)?"))
	}
}
