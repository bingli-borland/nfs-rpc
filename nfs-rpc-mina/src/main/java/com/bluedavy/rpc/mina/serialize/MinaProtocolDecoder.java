package com.bluedavy.rpc.mina.serialize;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

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
public class MinaProtocolDecoder extends CumulativeProtocolDecoder {

	private static final int REQUEST_HEADER_LEN = 1 * 7 + 5 * 4;
	
	private static final int RESPONSE_HEADER_LEN = 1 * 7 + 2 * 4;
	
	private static final byte REQUEST = (byte)0;
	
	private static final byte RESPONSE = (byte)1;
	
	protected boolean doDecode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
		final int originPos = in.position();
        if(in.remaining() < 2){
        	in.position(originPos);
        	return false;
        }
        byte version = in.get();
        // �汾1Э��Ľ�����ʽ
        if(version == (byte)1){
        	byte type = in.get();
        	if(type == REQUEST){
        		if(in.remaining() < REQUEST_HEADER_LEN -2){
        			in.position(originPos);
        			return false;
        		}
        		in.get();
        		in.get();
        		in.get();
        		in.get();
        		in.get();
        		int requestId = in.getInt();
        		int timeout = in.getInt();
        		int targetInstanceLen = in.getInt();
        		int methodNameLen = in.getInt();
        		int argsCount = in.getInt();
        		int argInfosLen = argsCount * 4 * 2;
        		int expectedLen = argInfosLen + targetInstanceLen + methodNameLen;
        		if(in.remaining() < expectedLen){
        			in.position(originPos);
        			return false;
        		}
        		expectedLen = 0;
        		int[] argsTypeLen = new int[argsCount];
        		for (int i = 0; i < argsCount; i++) {
					argsTypeLen[i] = in.getInt();
					expectedLen += argsTypeLen[i]; 
				}
        		int[] argsLen = new int[argsCount];
        		for (int i = 0; i < argsCount; i++) {
        			argsLen[i] = in.getInt();
        			expectedLen += argsLen[i];
				}
        		byte[] targetInstanceByte = new byte[targetInstanceLen];
        		in.get(targetInstanceByte);
        		String targetInstanceName = new String(targetInstanceByte);
        		byte[] methodNameByte = new byte[methodNameLen];
        		in.get(methodNameByte);
        		String methodName = new String(methodNameByte);
        		if(in.remaining() < expectedLen){
        			in.position(originPos);
        			return false;
        		}
        		String[] argTypes = new String[argsCount];
        		for (int i = 0; i < argsCount; i++) {
					byte[] argTypeByte = new byte[argsTypeLen[i]];
					in.get(argTypeByte);
					argTypes[i] = new String(argTypeByte);
				}
        		Object[] args = new Object[argsCount];
        		for (int i = 0; i < argsCount; i++) {
					byte[] argByte = new byte[argsLen[i]];
					in.get(argByte);
					args[i] = argByte;
				}
        		RequestWrapper wrapper = new RequestWrapper(targetInstanceName, methodName, argTypes, args, timeout, requestId);
        		out.write(wrapper);
        	}
        	else if(type == RESPONSE){
        		if(in.remaining() < RESPONSE_HEADER_LEN -2){
        			in.position(originPos);
        			return false;
        		}
        		in.get();
            	in.get();
            	in.get();
            	in.get();
            	in.get();
            	int requestId = in.getInt();
            	int bodyLen = in.getInt();
            	if(in.remaining() < bodyLen){
            		in.position(originPos);
            		return false;
            	}
            	byte[] bodyBytes = new byte[bodyLen];
            	in.get(bodyBytes);
            	ResponseWrapper wrapper = new ResponseWrapper();
        		wrapper.setRequestId(requestId);
        		wrapper.setResponse(bodyBytes);
	        	out.write(wrapper);
        	}
        	else{
        		throw new UnsupportedOperationException("protocol type : "+type+" is not supported!");
        	}
        }
        else{
        	throw new UnsupportedOperationException("protocol version :"+version+" is not supported!");
        }
        return true;
	}

}
