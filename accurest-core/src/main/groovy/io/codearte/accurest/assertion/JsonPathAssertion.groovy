package io.codearte.accurest.assertion

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath

/**
 * @author Marcin Grzejszczak
 */
class JsonPathAssertion {

	private final DocumentContext parsedJson

	JsonPathAssertion(DocumentContext parsedJson) {
		this.parsedJson = parsedJson
	}

	static JsonPathAssertion assertThat(String body) {
		DocumentContext parsedJson = JsonPath.parse(body)
		return new JsonPathAssertion(parsedJson)
	}

	FieldAssertion field(String name) {
		return new FieldAssertion()
	}

	class FieldAssertion {

		private FieldAssertion() {}

		void isEqualTo(String value) {

		}
	}
}
