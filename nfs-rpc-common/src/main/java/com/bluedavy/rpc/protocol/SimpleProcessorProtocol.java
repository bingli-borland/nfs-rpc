package com.bluedavy.rpc.protocol;

import com.bluedavy.rpc.Coders;
import com.bluedavy.rpc.RequestWrapper;
import com.bluedavy.rpc.ResponseWrapper;

/**
 * Э���ʽ����
 * 	VERSION(1B):   Э��汾��
 *  TYPE(1B):      ����/��Ӧ 
 *  DATATYPE(1B):  ���л�����
 *  KEEPED(1B):    ����Э��ͷ1
 *  KEEPED(1B):    ����Э��ͷ2
 *  KEEPED(1B):    ����Э��ͷ3
 *  KEEPED(1B):    ����Э��ͷ4
 *  KEEPED(1B):    ����Э��ͷ5  // ��֤����
 *  ID(4B):        ����ID
 *  TIMEOUT(4B):   ����ʱʱ��
 *  LENGTH(4B):    ������
 *  BODY
 */
public class SimpleProcessorProtocol implements Protocol{
	
	private static final int HEADER_LEN = 1 * 8 + 3 * 4;
	
	private static final byte VERSION = (byte)1;
	
	private static final byte REQUEST = (byte)0;
	
	private static final byte RESPONSE = (byte)1;
	
	/**
	 * encode Message to byte & write to io framework
	 * 
	 * @param message
	 * @param byteBuffer
	 * @throws Exception
	 */
	public ByteBufferWrapper encode(Object message,ByteBufferWrapper bytebufferWrapper) throws Exception{
		if(!(message instanceof RequestWrapper) && !(message instanceof ResponseWrapper)){
			throw new Exception("only support send RequestWrapper && ResponseWrapper");
		}
		int id = 0;
		byte type = REQUEST;
		byte[] body = null;
		int timeout = 0;
		int dataType = 0;
		if(message instanceof RequestWrapper){
			try{
				RequestWrapper wrapper = (RequestWrapper) message;
				dataType = wrapper.getDataType();
				body = Coders.getEncoder(String.valueOf(dataType)).encode(wrapper.getMessage()); 
				id = wrapper.getId();
				timeout = wrapper.getTimeout();
			}
			catch(Exception e){
				e.printStackTrace();
				// TODO: �����쳣
				// LOGGER.error("serialize request object error",e);
				// TODO: ֱ�Ӵ���һ����Ӧ���أ�������Ҫ�ȵ���ʱ
				throw e;
			}
		}
		else{
			ResponseWrapper wrapper = (ResponseWrapper) message;
			try{
				dataType = wrapper.getDataType();
				body = Coders.getEncoder(String.valueOf(dataType)).encode(wrapper.getResponse()); 
				id = wrapper.getRequestId();
			}
			catch(Exception e){
				// TODO: �����쳣
				e.printStackTrace();
				// LOGGER.error("serialize response object error",e);
				// ��Ȼ������Ӧ�ͻ��ˣ��Ա�ͻ��˿��ٽӵ���Ӧ����Ӧ�Ĵ���
				wrapper.setResponse(new Exception("serialize response object error",e));
				body = Coders.getEncoder(String.valueOf(wrapper.getDataType())).encode(wrapper.getResponse()); 
			}
			type = RESPONSE;
		}
		int capacity = HEADER_LEN + body.length;
		ByteBufferWrapper byteBuffer = bytebufferWrapper.get(capacity);
		byteBuffer.writeByte(VERSION);
		byteBuffer.writeByte(type);
		byteBuffer.writeByte((byte)dataType);
		byteBuffer.writeByte((byte)0);
		byteBuffer.writeByte((byte)0);
		byteBuffer.writeByte((byte)0);
		byteBuffer.writeByte((byte)0);
		byteBuffer.writeByte((byte)0);
		byteBuffer.writeInt(id);
		byteBuffer.writeInt(timeout);
		byteBuffer.writeInt(body.length);
		byteBuffer.writeBytes(body);
		return byteBuffer;
	}
	
	/**
	 * decode stream to object
	 * 
	 * @param wrapper
	 * @param errorObject stream not enough,then return this object
	 * @return Object 
	 * @throws Exception
	 */
	public Object decode(ByteBufferWrapper wrapper,Object errorObject) throws Exception{
		final int originPos = wrapper.readerIndex();
		if(wrapper.readableBytes() < HEADER_LEN){
			wrapper.setReaderIndex(originPos);
        	return errorObject;
        }
        byte version = wrapper.readByte();
        // �汾1Э��Ľ�����ʽ
        if(version == (byte)1){
        	byte type = wrapper.readByte();
        	int dataType = wrapper.readByte();
    		wrapper.readByte();
    		wrapper.readByte();
    		wrapper.readByte();
    		wrapper.readByte();
    		wrapper.readByte();
    		int requestId = wrapper.readInt();
    		int timeout = wrapper.readInt();
    		int expectedLen = wrapper.readInt();
    		if(wrapper.readableBytes() < expectedLen){
    			wrapper.setReaderIndex(originPos);
    			return errorObject;
    		}
    		byte[] body = new byte[expectedLen];
    		wrapper.readBytes(body);
        	if(type == REQUEST){
        		RequestWrapper requestWrapper = new RequestWrapper(body,timeout,requestId,dataType);
        		return requestWrapper;
        	}
        	else if(type == RESPONSE){
        		ResponseWrapper responseWrapper = new ResponseWrapper();
            	responseWrapper.setRequestId(requestId);
            	responseWrapper.setResponse(body);
            	responseWrapper.setDataType(dataType);
	        	return responseWrapper;
        	}
        	else{
        		throw new UnsupportedOperationException("protocol type : "+type+" is not supported!");
        	}
        }
        else{
        	throw new UnsupportedOperationException("protocol version :"+version+" is not supported!");
        }
	}

}
