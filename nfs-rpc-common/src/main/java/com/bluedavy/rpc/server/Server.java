package com.bluedavy.rpc.server;

import java.util.concurrent.ExecutorService;

public interface Server {

	/**
	 * ָ��listenPort����Server
	 * 
	 * @param listenPort �����˿�
	 * @param businessThreadPool ҵ���̳߳�
	 * @throws Exception
	 */
	public void start(int listenPort,ExecutorService businessThreadPool) throws Exception;
	
	/**
	 * ע��ҵ������
	 * 
	 * @param serviceName
	 * @param serviceInstance
	 */
	public void registerProcessor(String serviceName,Object serviceInstance);
	
	/**
	 * ֹͣServer
	 * 
	 * @throws Exception
	 */
	public void stop() throws Exception;
	
}
