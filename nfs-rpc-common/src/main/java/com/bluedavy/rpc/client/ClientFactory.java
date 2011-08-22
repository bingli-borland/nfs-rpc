package com.bluedavy.rpc.client;

public interface ClientFactory {

	/**
	 * ����targetIP,targetPort��ȡClient���粻���ڣ��򴴽�һ����Ĭ��ÿ��targetIP :
	 * targetPortֻ�ᴴ��һ��Client
	 * 
	 * Ĭ���������targetIP : targetPort Ϊkey������Ҫ��������key�������Զ���Ĵ���
	 */
	public abstract Client get(final String targetIP, final int targetPort,
			final int connectTimeout, String... customKey) throws Exception;

	/**
	 * ����targetIP,targetPort��ȡClient���粻���ڣ��򴴽���Ĭ��ÿ��targetIP :
	 * targetPort����clientNums������Client instance
	 * 
	 * ѡ��ʱRandom
	 * 
	 * Ĭ���������targetIP : targetPort Ϊkey������Ҫ��������key�������Զ���Ĵ���
	 */
	public abstract Client get(final String targetIP, final int targetPort,
			final int connectTimeout, final int clientNums, String... customKey)
			throws Exception;

	/**
	 * �Ƴ���ĳ��key�µ�ĳ��Client
	 * 
	 * @param key
	 * @param client
	 */
	public abstract void removeClient(String key, Client client);

}