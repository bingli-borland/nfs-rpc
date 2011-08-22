package com.bluedavy.rpc;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * ���͵����������д�������ʱ����ֱ�Ӱ��������д�룬��˴˶�����֧�����л�
 */
public class RequestWrapper {
	
	private static AtomicInteger incId = new AtomicInteger(0);
	
	private String targetInstanceName;
	
	private String methodName;
	
	private String[] argTypes;
	
	private Object[] requestObjects = null;
	
	private int timeout = 0;
	
	private int id = 0;
	
	public RequestWrapper(String targetInstanceName,String methodName,String[] argTypes,Object[] requestObjects,int timeout){
		this.requestObjects = requestObjects;
		this.id = incId.incrementAndGet();
		this.timeout = timeout;
		this.targetInstanceName = targetInstanceName;
		this.methodName = methodName;
		this.argTypes = argTypes;
	}
	
	public RequestWrapper(String targetInstanceName,String methodName,String[] argTypes,
						  Object[] requestObjects,int timeout,int id){
		this.requestObjects = requestObjects;
		this.id = id;
		this.timeout = timeout;
		this.targetInstanceName = targetInstanceName;
		this.methodName = methodName;
		this.argTypes = argTypes;
	}

	public String getTargetInstanceName() {
		return targetInstanceName;
	}

	public String getMethodName() {
		return methodName;
	}

	public int getTimeout() {
		return timeout;
	}

	public Object[] getRequestObjects() {
		return requestObjects;
	}

	public int getId() {
		return id;
	}	
	
	public String[] getArgTypes() {
		return argTypes;
	}
	
}
