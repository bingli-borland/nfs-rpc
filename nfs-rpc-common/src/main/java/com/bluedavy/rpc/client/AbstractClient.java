package com.bluedavy.rpc.client;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.bluedavy.rpc.RequestWrapper;
import com.bluedavy.rpc.ResponseWrapper;

/**
 * Common Client
 */
public abstract class AbstractClient implements Client {

	private static final Log LOGGER = LogFactory.getLog(AbstractClient.class);
	
	private static final boolean isWarnEnabled = LOGGER.isWarnEnabled();
	
	private static final long PRINT_CONSUME_MINTIME = Long.parseLong(System.getProperty("nfs.rpc.print.consumetime","0"));
	
	protected static ConcurrentHashMap<Integer, ArrayBlockingQueue<ResponseWrapper>> responses = 
			new ConcurrentHashMap<Integer, ArrayBlockingQueue<ResponseWrapper>>();

	/*
	 * (non-Javadoc)
	 * @see com.bluedavy.rpc.Client#invokeSync(java.lang.Object, int)
	 */
	public Object invokeSync(Object message, int timeout) throws Exception {
		RequestWrapper wrapper = new RequestWrapper(message, timeout);
		return invokeSyncIntern(wrapper);
	}
	
	/*
	 * (non-Javadoc)
	 * @see com.bluedavy.rpc.Client#invokeSync(java.lang.String, java.lang.String, java.lang.String[], java.lang.Object[], int)
	 */
	public Object invokeSync(String targetInstanceName,String methodName,String[] argTypes,Object[] args,int timeout) 
		throws Exception {
		RequestWrapper wrapper = new RequestWrapper(targetInstanceName,methodName,argTypes,args, timeout);
		return invokeSyncIntern(wrapper);
	}
	
	private Object invokeSyncIntern(RequestWrapper wrapper) 
			throws Exception {
			long beginTime = System.currentTimeMillis();
			ArrayBlockingQueue<ResponseWrapper> responseQueue = new ArrayBlockingQueue<ResponseWrapper>(1);
			responses.put(wrapper.getId(), responseQueue);
			ResponseWrapper responseWrapper = null;
			try {
				sendRequest(wrapper, wrapper.getTimeout());
			}
			catch(Exception e){
				responses.remove(wrapper.getId());
				responseQueue = null;
				LOGGER.error("send request to os sendbuffer occurs error",e);
				throw e;
			}
			try{
				responseWrapper = responseQueue.poll(wrapper.getTimeout() - (System.currentTimeMillis() - beginTime),TimeUnit.MILLISECONDS);
				if(PRINT_CONSUME_MINTIME > 0 && isWarnEnabled){
					long consumeTime = System.currentTimeMillis() - beginTime;
					if(consumeTime > PRINT_CONSUME_MINTIME){
						LOGGER.warn("client.invokeSync consume time: " + consumeTime + 
									" ms, server is: " + getServerIP() + ":" + getServerPort() + " request id is:" + wrapper.getId());
					}
				}
				if (responseWrapper == null) {
					String errorMsg = "receive response timeout(" + wrapper.getTimeout() + 
									  " ms),server is: " + getServerIP() + ":" + getServerPort() + " request id is:" + wrapper.getId();
					LOGGER.error(errorMsg);
					throw new Exception(errorMsg);
				}
				// do deserialize in business threadpool
				if(responseWrapper.getResponse() instanceof byte[]){
					ObjectInputStream objectIn = new ObjectInputStream(new ByteArrayInputStream((byte[])responseWrapper.getResponse()));
		    		Object responseObject = objectIn.readObject();
		    		objectIn.close();
		    		if(responseObject instanceof Throwable){
		    			responseWrapper.setException((Throwable) responseObject);
		    		}
		    		else{
		    			responseWrapper.setResponse(responseObject);
		    		}
				}
				// �쳣����
				if (responseWrapper.isError()) {
					Throwable t = responseWrapper.getException();
					t.fillInStackTrace();
					String errorMsg = "server error,server is: " + getServerIP() + ":" + getServerPort() + 
									  " request id is:"+wrapper.getId();
					LOGGER.error(errorMsg,t);
					throw new Exception(errorMsg,t);
				}
			} 
			catch (InterruptedException e) {
				LOGGER.error("Get response error",e);
				throw new Exception("Get response error", e);
			} 
			finally {
				responses.remove(wrapper.getId());
			}
			return responseWrapper.getResponse();
		}

	/**
	 * ���շ��ص���Ӧ��
	 * 
	 * @param wrapper
	 * @throws Exception
	 */
	public void putResponse(ResponseWrapper wrapper) throws Exception {
		if (!responses.containsKey(wrapper.getRequestId())) {
			LOGGER.warn("give up the response,request id is:"+wrapper.getRequestId()+",maybe because timeout!");
			return;
		}
		try {
			ArrayBlockingQueue<ResponseWrapper> queue = responses.get(wrapper.getRequestId());
			if (queue != null) {
				queue.put(wrapper);
			}
			else{
				LOGGER.warn("give up the response,request id is:"+wrapper.getRequestId()+",because queue is null");
			}
		} 
		catch (InterruptedException e) {
			LOGGER.error("put response error,request id is:"+wrapper.getRequestId(),e);
			throw new Exception("put response error", e);
		}
	}

	/**
	 * �첽������������ⲽҪȷ����ȷ��д����os��sendBuffer���д��ʧ�ܣ����׳��쳣
	 * ͨ������AbstractClient��putResponse���Ż���Ӧ��Ϣ
	 * 
	 * @param wrapper
	 * @throws Exception
	 */
	public abstract void sendRequest(RequestWrapper wrapper, int timeout) throws Exception;

}
