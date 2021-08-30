package io.cornerstone.test.mock;

import static org.mockito.Mockito.spy;

import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class SpyResultCaptor<T> implements Answer<T> {

	private T result;

	public T getResult() {
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T answer(InvocationOnMock invocationOnMock) throws Throwable {
		result = spy((T) invocationOnMock.callRealMethod());
		return result;
	}

}