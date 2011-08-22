package com.bluedavy.rpc;

import com.bluedavy.rpc.protocol.Protocol;

/**
 * ��װ���յ�����Ӧ��Ϣ���Ը��õķֱ����������쳣����Ϣ
 */
public class ResponseWrapper {

	private int requestId = 0;
	
	private Object response = null;
	
	private boolean isError = false;
	
	private Throwable exception = null;
	
	private int dataType = Protocol.JAVA_DATA;

	public int getDataType() {
		return dataType;
	}

	public void setDataType(int dataType) {
		this.dataType = dataType;
	}

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
