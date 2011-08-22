package com.bluedavy.rpc.netty.serialize;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

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
 *  KEEPED(1B):    ����Э��ͷ6  // ��֤����
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
 *  KEEPED(1B):    ����Э��ͷ6  // ��֤����
 *  ID(4B):        ����ID
 *  LENGTH(4B):    ������
 *  BODY
 */
public class NettyProtocolEncoder extends OneToOneEncoder {

	private static final int REQUEST_HEADER_LEN = 1 * 7 + 5 * 4;
	
	private static final int RESPONSE_HEADER_LEN = 1 * 7 + 2 * 4;
	
	private static final byte VERSION = (byte)1;
	
	private static final byte REQUEST = (byte)0;
	
	private static final byte RESPONSE = (byte)1;
	
	protected Object encode(ChannelHandlerContext ctx, Channel channel,Object message) throws Exception {
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
				ChannelBuffer byteBuffer = ChannelBuffers.dynamicBuffer(capacity);
				byteBuffer.writeByte(VERSION);
				byteBuffer.writeByte(type);
				byteBuffer.writeByte((byte)0);
				byteBuffer.writeByte((byte)0);
				byteBuffer.writeByte((byte)0);
				byteBuffer.writeByte((byte)0);
				byteBuffer.writeByte((byte)0);
				byteBuffer.writeByte((byte)0);
				byteBuffer.writeInt(id);
				byteBuffer.writeInt(timeout);
				byteBuffer.writeInt(targetInstanceNameByte.length);
				byteBuffer.writeInt(methodNameByte.length);
				byteBuffer.writeInt(requestArgs.size());
				for (byte[] requestArgType : requestArgTypes) {
					byteBuffer.writeInt(requestArgType.length);
				}
				for (byte[] requestArg : requestArgs) {
					byteBuffer.writeInt(requestArg.length);
				}
				byteBuffer.writeBytes(targetInstanceNameByte);
				byteBuffer.writeBytes(methodNameByte);
				for (byte[] requestArgType : requestArgTypes) {
					byteBuffer.writeBytes(requestArgType);
				}
				for (byte[] requestArg : requestArgs) {
					byteBuffer.writeBytes(requestArg);
				}
				return byteBuffer;
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
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			try{
				ObjectOutputStream output = new ObjectOutputStream(byteArray);
				output.writeObject(wrapper.getResponse());
				output.flush();
				output.close();
				id = wrapper.getRequestId();
			}
			catch(Exception e){
				// TODO: �����쳣
				e.printStackTrace();
				// LOGGER.error("serialize response object error",e);
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
			ChannelBuffer byteBuffer = ChannelBuffers.dynamicBuffer(capacity);
			byteBuffer.writeByte(VERSION);
			byteBuffer.writeByte(type);
			byteBuffer.writeByte((byte)0);
			byteBuffer.writeByte((byte)0);
			byteBuffer.writeByte((byte)0);
			byteBuffer.writeByte((byte)0);
			byteBuffer.writeByte((byte)0);
			byteBuffer.writeByte((byte)0);
			byteBuffer.writeInt(id);
			byteBuffer.writeInt(body.length);
			byteBuffer.writeBytes(body);
			return byteBuffer;
		}
	}

}
