package io.codearte.accurest.dsl.internal

class OptionalProperty extends PatternProperty {

	OptionalProperty(Object value) {
		super("($value)?")
	}
}
