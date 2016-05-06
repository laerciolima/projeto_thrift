import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.thrift.TException;

import lamedb.KeyValue;
import lamedb.LameDB.Iface;
import lamedb.ValueIsTooLongException;

public class ServerHandler implements Iface {

	private ConcurrentHashMap<Long, KeyValue> valores = new ConcurrentHashMap<>();

	// Retorna KeyValue com versão -1, caso chave não esteja no banco.
	// Retorna o par key/value caso chave esteja no banco.
	@Override
	public KeyValue get(long key) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	// Retorna uma lista com todas as chaves entre keyBegin e keyEnd, inclusive.
	// Caso não existam chaves na faixa, a lista é vazia.
	@Override
	public List<KeyValue> getRange(long keyBegin, long keyEnd) throws TException {
		// TODO Auto-generated method stub
		return null;
	}

	// Caso a chave não exista no banco, insere par com version_t = 0 e retorna
	// 0
	// Caso a chave exista no banco, retorna a versão atual.
	@Override
	public int put(long key, ByteBuffer value) throws ValueIsTooLongException, TException {
		// TODO Auto-generated method stub
		System.out.print(key + " entrou aqui: ");
		KeyValue k;
		if ((k = valores.get(key)) != null) {
			k.setVersion(k.getVersion() + 1);
			k.setValue(value);
		} else {
			k = new KeyValue();
			k.key = key;
			k.value = value;
			k.version = 0;
			valores.put(key, k);
		}
		System.out.println(k.version);
		return k.version;
	}

	// Caso a chave exista no banco, sobrescreve, incrementando a versão em 1
	// e retorna a nova versão
	// Caso a chave não exista no banco, retorna -1 (versão inválida)
	@Override
	public int update(long key, ByteBuffer value) throws ValueIsTooLongException, TException {
		// TODO Auto-generated method stub
		return 0;
	}

	// Caso a chave exista no banco, com a chave igual ao parâmetro version,
	// sobrescreve, incrementando a versão e retorna a nova versão
	// Caso a chave não exista no banco, retorna -1 (versão inválida)
	@Override
	public int updateWithVersion(long key, ByteBuffer value, int version) throws ValueIsTooLongException, TException {
		// TODO Auto-generated method stub
		return 0;
	}

	// Caso a chave exista no banco, remove o par e o retorna
	// Caso a chave não exista no banco, retorna KeyValue com versao -1
	@Override
	public KeyValue remove(long key) throws TException {
		// TODO Auto-generated method stub
		
		KeyValue k = valores.remove(key);
		if(k ==  null){
			k = new KeyValue();
			k.version = -1;
		}
		return null;
	}

	// Caso a chave exista no banco, com chave igual ao parâmetro version
	// remove o par e o retorna
	// Caso a chave não exista no banco, retorna null
	@Override
	public KeyValue removeWithVersion(long key, int version) throws TException {
		// TODO Auto-generated method stub
		KeyValue k = valores.get(key);
		if(k.version == version)
			valores.remove(key);
		
		return k;
	}

}
