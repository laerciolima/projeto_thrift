import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;

import lamedb.KeyValue;
import lamedb.LameDB;
import lamedb.LameDB.Iface;
import lamedb.ValueIsTooLongException;

public class ServerHandler implements Iface {

	private ConcurrentHashMap<Long, KeyValue> valores = new ConcurrentHashMap<>();
	private long limite;
	private TreeSet<No> nodes;
	ZkConnect conn = new ZkConnect("localhost", 2181);

	public ServerHandler(long limite, TreeSet<No> nodes, ConcurrentHashMap<Long, KeyValue> valores) {
		super();
		this.limite = limite;
		this.nodes = nodes;
		this.valores = valores;
	}

	// Retorna KeyValue com versão -1, caso chave não esteja no banco.
	// Retorna o par key/value caso chave esteja no banco.
	@Override
	public KeyValue get(long key) throws TException {
		// TODO Auto-generated method stub
		System.out.println("meu limte: " + limite);
		for (Map.Entry<Long, KeyValue> pair : valores.entrySet()) {
			System.out.print(pair.getKey() + " : ");
			System.out.println(pair.getValue().version);
		}

		No n = tratarRequisicao(key);
		if (n != null) {
			Dispatcher d = new Dispatcher(n.getIp(), n.getPorta());

			return d.get(key);
		}

		KeyValue k;
		if ((k = valores.get(key)) == null) {
			k = new KeyValue();
			k.version = -1;
		}
		return k;
	}

	// Retorna uma lista com todas as chaves entre keyBegin e keyEnd, inclusive.
	// Caso não existam chaves na faixa, a lista é vazia.
	@Override
	public List<KeyValue> getRange(long keyBegin, long keyEnd) throws TException {
		System.out.println("aquiii");
		No n;
		ArrayList<KeyValue> lista = new ArrayList<>();
		
		while ((n = tratarRequisicao(keyBegin)) != null) {
			
			Dispatcher d = new Dispatcher(n.getIp(), n.getPorta());
			lista.addAll(d.getRange(keyBegin, n.getLimite()));
			if (keyEnd > n.getLimite()){
				keyBegin = n.getLimite();
				d.fecharConexao();
			}else {
				return lista;
			}

		}

		for (long i = keyBegin; i <= keyEnd; i++) {
			if (valores.containsKey(i)){
				lista.add(valores.get(i));
			}
		}
		
		if(limite < keyEnd){
			keyBegin = limite;
			while ((n = tratarRequisicao(keyBegin)) != null) {
				Dispatcher d = new Dispatcher(n.getIp(), n.getPorta());
				lista.addAll(d.getRange(keyBegin, keyEnd));
				if (keyEnd > n.getLimite())
					keyBegin = n.getLimite();
				else {
					break;
				}

			}
		}

		return lista;

	}

	// Caso a chave não exista no banco, insere par com version_t = 0 e retorna
	// 0
	// Caso a chave exista no banco, retorna a versão atual.
	@Override
	public int put(long key, ByteBuffer value) throws ValueIsTooLongException, TException {
		// TODO Auto-generated method stub
		int r = 0;
		No n = tratarRequisicao(key);
		if (n != null) {

			Dispatcher d = new Dispatcher(n.getIp(), n.getPorta());
			System.out.print("dispatcher criado: ");
			return d.put(key, value);
		}

		System.out.println("continuando");

		KeyValue k;
		if ((k = valores.get(key)) != null) {
			k.setVersion(k.getVersion());
			k.setValue(value);
		} else {
			k = new KeyValue();
			k.key = key;
			k.value = value;
			k.version = 0;
			valores.put(key, k);
		}
		return k.version;
	}

	// Caso a chave exista no banco, sobrescreve, incrementando a versão em 1
	// e retorna a nova versão
	// Caso a chave não exista no banco, retorna -1 (versão inválida)
	@Override
	public int update(long key, ByteBuffer value) throws ValueIsTooLongException, TException {
		// TODO Auto-generated method stub

		No n = tratarRequisicao(key);
		if (n != null) {
			Dispatcher d = new Dispatcher(n.getIp(), n.getPorta());

			return d.update(key, value);
		}

		KeyValue k;
		if ((k = valores.get(key)) != null) {
			k.value = value;
			k.version++;
		} else {
			return -1;
		}
		return k.version;
	}

	// Caso a chave exista no banco, com a chave igual ao parâmetro version,
	// sobrescreve, incrementando a versão e retorna a nova versão
	// Caso a chave não exista no banco, retorna -1 (versão inválida)
	@Override
	public int updateWithVersion(long key, ByteBuffer value, int version) throws ValueIsTooLongException, TException {
		// TODO Auto-generated method stub

		No n = tratarRequisicao(key);
		if (n != null) {
			Dispatcher d = new Dispatcher(n.getIp(), n.getPorta());

			return d.updateWithVersion(key, value, version);
		}

		KeyValue k;

		if ((k = valores.get(key)) != null && k.version == version) {
			k.value = value;
			k.version++;
		} else
			return -1;

		return k.version;
	}

	// Caso a chave exista no banco, remove o par e o retorna
	// Caso a chave não exista no banco, retorna KeyValue com versao -1
	@Override
	public KeyValue remove(long key) throws TException {
		// TODO Auto-generated method stub

		No n = tratarRequisicao(key);
		if (n != null) {
			Dispatcher d = new Dispatcher(n.getIp(), n.getPorta());

			return d.remove(key);
		}

		KeyValue k = valores.remove(key);
		if (k == null) {
			k = new KeyValue();
			k.key = key;
			k.version = -1;
		}
		return k;
	}

	// Caso a chave exista no banco, com chave igual ao parâmetro version
	// remove o par e o retorna
	// Caso a chave não exista no banco, retorna null
	@Override
	public KeyValue removeWithVersion(long key, int version) throws TException {
		// TODO Auto-generated method stub

		No n = tratarRequisicao(key);
		if (n != null) {
			Dispatcher d = new Dispatcher(n.getIp(), n.getPorta());

			return d.removeWithVersion(key, version);
		}

		KeyValue k;

		if (((k = valores.get(key)) != null) && (k.version == version)) {
			valores.remove(key);
		} else
			return null;

		return k;

	}


	public No tratarRequisicao(long key) {
		
		
		nodes = conn.getNodes("/nodes");
		
		
		for (No no : nodes) {
			if (key < no.getLimite()) {

				if (no.getLimite() == limite)

					break; // proprio no

				System.out.println("Despachando para:" + no.getId());

				return no;
			}
		}
		if (nodes.last().getLimite() < key && nodes.first().getLimite() != limite)
			return nodes.first();

		return null;
	}

}
