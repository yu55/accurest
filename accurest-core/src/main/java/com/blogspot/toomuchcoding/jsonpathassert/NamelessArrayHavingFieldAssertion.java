package com.blogspot.toomuchcoding.jsonpathassert;

import com.jayway.jsonpath.DocumentContext;

class NamelessArrayHavingFieldAssertion extends FieldAssertion {
	protected NamelessArrayHavingFieldAssertion(DocumentContext parsedJson,
			StringBuffer jsonPathBuffer, Object fieldName) {
		super(parsedJson, jsonPathBuffer, fieldName);
	}

	@Override
	public boolean isIteratingOverNamelessArray() {
		return true;
	}

}