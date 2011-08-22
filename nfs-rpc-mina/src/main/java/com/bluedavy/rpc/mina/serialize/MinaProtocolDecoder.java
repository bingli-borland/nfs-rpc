package com.bluedavy.rpc.mina.serialize;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;

import com.bluedavy.rpc.RPCProtocolUtil;
/**
 * decode byte[]
 */
public class MinaProtocolDecoder extends CumulativeProtocolDecoder {
	
	protected boolean doDecode(IoSession session, ByteBuffer in, ProtocolDecoderOutput out) throws Exception {
		MinaByteBufferWrapper wrapper = new MinaByteBufferWrapper(in);
		Object returnObject = RPCProtocolUtil.decode(wrapper, false);
		if(returnObject instanceof Boolean){
			return false;
		}
		out.write(returnObject);
		return true;
	}

}
