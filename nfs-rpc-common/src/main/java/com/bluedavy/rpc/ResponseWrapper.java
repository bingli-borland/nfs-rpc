package com.bluedavy.rpc;
/**
 * ��װ���յ�����Ӧ��Ϣ���Ը��õķֱ����������쳣����Ϣ
 */
public class ResponseWrapper {

	private int requestId = 0;
	
	private Object response = null;
	
	private boolean isError = false;
	
	private Throwable exception = null;

	public int getRequestId() {
		return requestId;
	}

	public void setRequestId(int requestId) {
		this.requestId = requestId;
	}

	public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}

	public boolean isError() {
		return isError;
	}

	public Throwable getException() {
		return exception;
	}

	public void setException(Throwable exception) {
		this.exception = exception;
		isError = true;
	}
	
}
