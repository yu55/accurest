package io.codearte.accurest.util;

import com.blogspot.toomuchcoding.jsonpathassert.JsonPathVerifiable;

/**
 * @author Marcin Grzejszczak
 */
class DelegatingJsonPathVerifiable implements MethodBufferingJsonPathVerifiable {

	private final JsonPathVerifiable delegate;
	private final StringBuffer methodsBuffer;

	DelegatingJsonPathVerifiable(JsonPathVerifiable delegate,
			StringBuffer methodsBuffer) {
		this.delegate = delegate;
		this.methodsBuffer = new StringBuffer(methodsBuffer.toString());
	}

	DelegatingJsonPathVerifiable(JsonPathVerifiable delegate) {
		this.delegate = delegate;
		this.methodsBuffer = new StringBuffer();
	}

	private static String stringWithEscapedQuotes(Object object) {
		String stringValue = object.toString();
		return stringValue.replaceAll("\"", "\\\\\"");
	}

	private static String wrapValueWithQuotes(Object value) {
		return value instanceof String ?
				"\"" + stringWithEscapedQuotes(value) + "\"" :
				value.toString();
	}

	@Override
	public MethodBufferingJsonPathVerifiable contains(Object value) {
		DelegatingJsonPathVerifiable verifiable = new DelegatingJsonPathVerifiable(delegate.contains(value), methodsBuffer);
		verifiable.methodsBuffer.append(".contains(").append(wrapValueWithQuotes(value))
				.append(")");
		if (isAssertingAValueInArray()) {
			verifiable.methodsBuffer.append(".value()");
		}
		return verifiable;
	}

	@Override
	public MethodBufferingJsonPathVerifiable field(Object value) {
		DelegatingJsonPathVerifiable verifiable = new DelegatingJsonPathVerifiable(delegate.field(value), methodsBuffer);
		verifiable.methodsBuffer.append(".field(").append(wrapValueWithQuotes(value))
				.append(")");
		return verifiable;
	}

	@Override
	public MethodBufferingJsonPathVerifiable fieldBeforeMatching(Object value) {
		DelegatingJsonPathVerifiable verifiable = new DelegatingJsonPathVerifiable(delegate.fieldBeforeMatching(value), methodsBuffer);
		if (delegate.isIteratingOverArray()) {
			verifiable.methodsBuffer.append(".contains(").append(wrapValueWithQuotes(value))
					.append(")");
		} else {
			verifiable.methodsBuffer.append(".field(").append(wrapValueWithQuotes(value))
					.append(")");
		}
		return verifiable;
	}

	@Override
	public MethodBufferingJsonPathVerifiable array(Object value) {
		DelegatingJsonPathVerifiable verifiable = new DelegatingJsonPathVerifiable(delegate.array(value), methodsBuffer);
		verifiable.methodsBuffer.append(".array(").append(wrapValueWithQuotes(value))
				.append(")");
		return verifiable;
	}

	@Override
	public MethodBufferingJsonPathVerifiable arrayField(Object value) {
		DelegatingJsonPathVerifiable verifiable = new DelegatingJsonPathVerifiable(delegate.arrayField(value), methodsBuffer);
		verifiable.methodsBuffer.append(".array(").append(wrapValueWithQuotes(value))
				.append(")");
		return verifiable;
	}

	@Override
	public MethodBufferingJsonPathVerifiable arrayField() {
		return new DelegatingJsonPathVerifiable(delegate.arrayField(), methodsBuffer);
	}

	@Override
	public MethodBufferingJsonPathVerifiable array() {
		DelegatingJsonPathVerifiable verifiable = new DelegatingJsonPathVerifiable(delegate.array(), methodsBuffer);
		verifiable.methodsBuffer.append(".array()");
		return verifiable;
	}

	@Override
	public MethodBufferingJsonPathVerifiable iterationPassingArray() {
		return new DelegatingJsonPathVerifiable(delegate.iterationPassingArray(), methodsBuffer);
	}

	@Override
	public MethodBufferingJsonPathVerifiable isEqualTo(String value) {
		DelegatingJsonPathVerifiable readyToCheck = new DelegatingJsonPathVerifiable(delegate.isEqualTo(value), methodsBuffer);
		if (delegate.isAssertingAValueInArray()) {
			readyToCheck.methodsBuffer.append(".value()");
		} else {
			readyToCheck.methodsBuffer.append(".isEqualTo(")
					.append(wrapValueWithQuotes(value)).append(")");
		}
		return readyToCheck;
	}

	@Override
	public MethodBufferingJsonPathVerifiable isEqualTo(Object value) {
		if (value == null) {
			return isNull();
		}
		return isEqualTo(value.toString());
	}

	@Override
	public MethodBufferingJsonPathVerifiable isEqualTo(Number value) {
		DelegatingJsonPathVerifiable readyToCheck = new DelegatingJsonPathVerifiable(delegate.isEqualTo(value), methodsBuffer);
		if (delegate.isAssertingAValueInArray()) {
			readyToCheck.methodsBuffer.append(".value()");
		} else {
			readyToCheck.methodsBuffer.append(".isEqualTo(").append(String.valueOf(value))
					.append(")");
		}
		return readyToCheck;
	}

	@Override
	public MethodBufferingJsonPathVerifiable isNull() {
		DelegatingJsonPathVerifiable readyToCheck = new DelegatingJsonPathVerifiable(delegate.isNull(), methodsBuffer);
		readyToCheck.methodsBuffer.append(".isNull()");
		return readyToCheck;
	}

	@Override
	public MethodBufferingJsonPathVerifiable matches(String value) {
		DelegatingJsonPathVerifiable readyToCheck = new DelegatingJsonPathVerifiable(delegate.matches(value), methodsBuffer);
		if (delegate.isAssertingAValueInArray()) {
			readyToCheck.methodsBuffer.append(".value()");
		} else {
			readyToCheck.methodsBuffer.append(".matches(").append(wrapValueWithQuotes(value))
					.append(")");
		}
		return readyToCheck;
	}

	@Override
	public MethodBufferingJsonPathVerifiable isEqualTo(Boolean value) {
		DelegatingJsonPathVerifiable readyToCheck = new DelegatingJsonPathVerifiable(delegate.isEqualTo(value), methodsBuffer);
		if (delegate.isAssertingAValueInArray()) {
			readyToCheck.methodsBuffer.append(".value()");
		} else {
			readyToCheck.methodsBuffer.append(".isEqualTo(").append(String.valueOf(value))
					.append(")");
		}
		return readyToCheck;
	}

	@Override
	public MethodBufferingJsonPathVerifiable value() {
		return new DelegatingJsonPathVerifiable(delegate, methodsBuffer);
	}

	@Override
	public String jsonPath() {
		return delegate.jsonPath();
	}

	@Override
	public boolean isReadyToCheck() {
		return delegate.isReadyToCheck();
	}

	@Override
	public void check() {
		delegate.check();
	}

	@Override
	public boolean isIteratingOverNamelessArray() {
		return delegate.isIteratingOverNamelessArray();
	}

	@Override
	public boolean isIteratingOverArray() {
		return delegate.isIteratingOverArray();
	}

	@Override
	public boolean isAssertingAValueInArray() {
		return delegate.isAssertingAValueInArray();
	}

	@Override
	public String method() {
		return methodsBuffer.toString();
	}

	@Override
	public String methodWithAppendedCheck() {
		return methodsBuffer.append(".check();").toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		DelegatingJsonPathVerifiable that = (DelegatingJsonPathVerifiable) o;

		if (delegate != null ? !delegate.equals(that.delegate) : that.delegate != null)
			return false;
		return methodsBuffer != null ?
				methodsBuffer.equals(that.methodsBuffer) :
				that.methodsBuffer == null;

	}

	@Override
	public int hashCode() {
		int result = delegate != null ? delegate.hashCode() : 0;
		result = 31 * result + (methodsBuffer != null ? methodsBuffer.hashCode() : 0);
		return result;
	}

	@Override
	public String toString() {
		return "DelegatingJsonPathVerifiable{" +
				"delegate=\n" + delegate +
				", methodsBuffer=" + methodsBuffer +
				'}';
	}
}
