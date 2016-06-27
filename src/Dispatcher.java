
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import com.sun.corba.se.impl.oa.poa.ActiveObjectMap.Key;

import java.nio.ByteBuffer;
import java.util.*;

import lamedb.*;

public class Dispatcher {

	TTransport transport;

	TProtocol protocol;
	LameDB.Client client;

	int porta;
	String host;

	public Dispatcher(String ip, int porta) {
		transport = new TSocket(ip, porta);
		this.porta = porta;
		this.host = ip;
		try {
			transport.open();
			protocol = new TBinaryProtocol(transport);
			client = new LameDB.Client(protocol);
		} catch (TTransportException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void fecharConexao() {
		transport.close();
	}

	public KeyValue get(long key) throws TException {

		return client.get(key);

	}

	public List<KeyValue> getRange(long start, long end) throws TException {

		return client.getRange(start, end);

	}

	public int put(long key, ByteBuffer value) throws TException {
		int r = 0;

		r = client.put(key, value);
		transport.close();

		return r;

	}

	public int update(long key, ByteBuffer value) throws TException {
		return client.update(key, value);

	}

	public int updateWithVersion(long key, ByteBuffer value, int version) throws TException {
		return client.updateWithVersion(key, value, version);
	}

	public KeyValue remove(long key) throws TException {
		return client.remove(key);

	}

	public KeyValue removeWithVersion(long key, int version) throws TException {
		return client.removeWithVersion(key, version);

	}

}
