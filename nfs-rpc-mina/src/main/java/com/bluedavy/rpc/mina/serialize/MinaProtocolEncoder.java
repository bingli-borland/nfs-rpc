package com.bluedavy.rpc.mina.serialize;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;

import com.bluedavy.rpc.RequestWrapper;
import com.bluedavy.rpc.ResponseWrapper;
/**
 * ����Э���ʽ����
 * 	VERSION(1B):   Э��汾��
 *  TYPE(1B):      ����/��Ӧ 
 *  KEEPED(1B):    ����Э��ͷ1
 *  KEEPED(1B):    ����Э��ͷ2
 *  KEEPED(1B):    ����Э��ͷ3
 *  KEEPED(1B):    ����Э��ͷ4
 *  KEEPED(1B):    ����Э��ͷ5
 *  ID(4B):        ����ID
 *  TIMEOUT(4B):   ����ʱʱ��
 *  TARGETINSTANCELEN(4B):  Ŀ�����Ƶĳ���
 *  METHODNAMELEN(4B):      �������ĳ���
 *  ARGSCOUNT(4B):          ������������
 *  ARG1TYPELEN(4B):        ��������1���͵ĳ���
 *  ARG2TYPELEN(4B):        ��������2���͵ĳ���
 *  ...
 *  ARG1LEN(4B):            ��������1�ĳ���
 *  ARG2LEN(4B):            ��������2�ĳ���
 *  ...
 *  TARGETINSTANCENAME
 *  METHODNAME
 *  ARG1TYPENAME
 *  ARG2TYPENAME
 *  ...
 *  ARG1
 *  ARG2
 *  ...
 *  
 * ��ӦЭ���ʽ����
 *  VERSION(1B):   Э��汾��
 *  TYPE(1B):      ����/��Ӧ 
 *  KEEPED(1B):    ����Э��ͷ1
 *  KEEPED(1B):    ����Э��ͷ2
 *  KEEPED(1B):    ����Э��ͷ3
 *  KEEPED(1B):    ����Э��ͷ4
 *  KEEPED(1B):    ����Э��ͷ5
 *  ID(4B):        ����ID
 *  LENGTH(4B):    ������
 *  BODY
 */
public class MinaProtocolEncoder extends ProtocolEncoderAdapter {

	private static final Log LOGGER = LogFactory.getLog(MinaProtocolEncoder.class);
	
	private static final int REQUEST_HEADER_LEN = 1 * 7 + 5 * 4;
	
	private static final int RESPONSE_HEADER_LEN = 1 * 7 + 2 * 4;
	
	private static final byte VERSION = (byte)1;
	
	private static final byte REQUEST = (byte)0;
	
	private static final byte RESPONSE = (byte)1;
	
	public void encode(IoSession session, Object message, ProtocolEncoderOutput out) throws Exception {
		if(!(message instanceof RequestWrapper) && !(message instanceof ResponseWrapper)){
			throw new Exception("only support send RequestWrapper && ResponseWrapper");
		}
		int id = 0;
		byte type = REQUEST;
		if(message instanceof RequestWrapper){
			try{
				int requestArgTypesLen = 0;
				int requestArgsLen = 0;
				List<byte[]> requestArgTypes = new ArrayList<byte[]>();
				List<byte[]> requestArgs = new ArrayList<byte[]>();
				RequestWrapper wrapper = (RequestWrapper) message;
				String[] requestArgTypeStrings = wrapper.getArgTypes();
				for (String requestArgType : requestArgTypeStrings) {
					byte[] argTypeByte = requestArgType.getBytes();
					requestArgTypes.add(argTypeByte);
					requestArgTypesLen += argTypeByte.length;
				}
				Object[] requestObjects = wrapper.getRequestObjects();
				for (Object requestArg : requestObjects) {
					ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
					ObjectOutputStream output = new ObjectOutputStream(byteArray);
					output.writeObject(requestArg);
					output.flush();
					output.close();
					byte[] requestArgByte = byteArray.toByteArray(); 
					requestArgs.add(requestArgByte);
					requestArgsLen += requestArgByte.length;
				}
				byte[] targetInstanceNameByte = wrapper.getTargetInstanceName().getBytes();
				byte[] methodNameByte = wrapper.getMethodName().getBytes();
				id = wrapper.getId();
				int timeout = wrapper.getTimeout();
				int capacity = REQUEST_HEADER_LEN + requestArgTypesLen + requestArgsLen;
				ByteBuffer byteBuffer = ByteBuffer.allocate(capacity,false);
				byteBuffer.put(VERSION);
				byteBuffer.put(type);
				byteBuffer.put((byte)0);
				byteBuffer.put((byte)0);
				byteBuffer.put((byte)0);
				byteBuffer.put((byte)0);
				byteBuffer.put((byte)0);
				byteBuffer.putInt(id);
				byteBuffer.putInt(timeout);
				byteBuffer.putInt(targetInstanceNameByte.length);
				byteBuffer.putInt(methodNameByte.length);
				byteBuffer.putInt(requestArgs.size());
				for (byte[] requestArgType : requestArgTypes) {
					byteBuffer.putInt(requestArgType.length);
				}
				for (byte[] requestArg : requestArgs) {
					byteBuffer.putInt(requestArg.length);
				}
				byteBuffer.put(targetInstanceNameByte);
				byteBuffer.put(methodNameByte);
				for (byte[] requestArgType : requestArgTypes) {
					byteBuffer.put(requestArgType);
				}
				for (byte[] requestArg : requestArgs) {
					byteBuffer.put(requestArg);
				}
				byteBuffer.flip();
				out.write(byteBuffer);
			}
			catch(Exception e){
				LOGGER.error("serialize request object error",e);
				// TODO: ֱ�Ӵ���һ����Ӧ���أ�������Ҫ�ȵ���ʱ
				throw e;
			}
		}
		else{
			ResponseWrapper wrapper = (ResponseWrapper) message;
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try{
				ObjectOutputStream output = new ObjectOutputStream(byteArray);
				output.writeObject(wrapper.getResponse());
				output.flush();
				output.close();
				id = wrapper.getRequestId();
			}
			catch(Exception e){
				LOGGER.error("serialize response object error",e);
				// ��Ȼ������Ӧ�ͻ��ˣ��Ա�ͻ��˿��ٽӵ���Ӧ����Ӧ�Ĵ���
				wrapper.setResponse(new Exception("serialize response object error",e));
				ObjectOutputStream output = new ObjectOutputStream(byteArray);
				output.writeObject(wrapper.getResponse());
				output.flush();
				output.close();
			}
			type = RESPONSE;
			byte[] body = byteArray.toByteArray();
			int capacity = RESPONSE_HEADER_LEN + body.length;
			ByteBuffer byteBuffer = ByteBuffer.allocate(capacity,false);
			byteBuffer.put(VERSION);
			byteBuffer.put(type);
			byteBuffer.put((byte)0);
			byteBuffer.put((byte)0);
			byteBuffer.put((byte)0);
			byteBuffer.put((byte)0);
			byteBuffer.put((byte)0);
			byteBuffer.putInt(id);
			byteBuffer.putInt(body.length);
			byteBuffer.put(body);
			byteBuffer.flip();
			out.write(byteBuffer);
		}
	}

}
