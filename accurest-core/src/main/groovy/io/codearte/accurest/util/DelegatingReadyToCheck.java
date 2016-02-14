package io.codearte.accurest.util;

import com.blogspot.toomuchcoding.jsonpathassert.ReadyToCheck;

/**
 * @author Marcin Grzejszczak
 */
class DelegatingReadyToCheck implements MethodBufferingReadyToCheck {

	private final ReadyToCheck delegate;
	final StringBuffer methodsBuffer;

	DelegatingReadyToCheck(ReadyToCheck delegate, StringBuffer methodsBuffer) {
		this.delegate = delegate;
		this.methodsBuffer = methodsBuffer;
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
	public String method() {
		return methodsBuffer.toString();
	}

	@Override
	public String methodWithAppendedCheck() {
		return methodsBuffer.append(".check();").toString();
	}
}
