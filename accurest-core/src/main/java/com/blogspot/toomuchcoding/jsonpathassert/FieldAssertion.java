package com.blogspot.toomuchcoding.jsonpathassert;

import com.jayway.jsonpath.DocumentContext;

class FieldAssertion extends JsonPathAsserter {
		protected FieldAssertion( DocumentContext parsedJson,
				StringBuffer jsonPathBuffer, StringBuffer methodsBuffer, Object fieldName) {
			super(parsedJson, jsonPathBuffer, methodsBuffer, fieldName);
		}
	}