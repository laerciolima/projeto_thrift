import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TServer.Args;
import org.apache.thrift.server.TSimpleServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TSSLTransportFactory;
import org.apache.thrift.transport.TSSLTransportFactory.TSSLTransportParameters;

import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

import com.sun.corba.se.impl.oa.poa.ActiveObjectMap.Key;

import lamedb.KeyValue;
import lamedb.LameDB;

public class JavaServer {

	public static ServerHandler handler;
	static int PORT;
	static long faixa;
	static final String DIR = "/nodes";
	public static TreeSet<No> nodes = new TreeSet<>();
	public static LameDB.Processor processor;

	public static void main(String[] args) {
		try {
			int id = 0;
			BufferedReader bf = new BufferedReader(new FileReader(new File("gambi")));
			String linha = "";
			while ((linha = bf.readLine()) != null) {
				id = Integer.parseInt(linha);
			}
			bf.close();

			BufferedWriter bw = new BufferedWriter(new FileWriter(new File("gambi")));
			if (id == 5) {
				bw.write("1");
			} else {
				bw.write("" + (++id));
				id--;
			}

			bw.close();

			// int id = Integer.parseInt(args[0]);

			ZkConnect conn = new ZkConnect("127.0.0.1", 2181);

			nodes = conn.getNodes(DIR);

			// varrer os nos a procura da maior faixa;
			ConcurrentHashMap<Long, KeyValue> valores = new ConcurrentHashMap<>();
			if (nodes.isEmpty()) {
				conn.createNode(DIR + "/node" + id, (id + " localhost " + (6500 + id) + " " + Long.MAX_VALUE + "").getBytes());
			} else {
				long maior_faixa = 0;
				long limite_anterior = 0;
				No no_aux = null;
				for (No no : nodes) {
					if (maior_faixa < (no.getLimite() - limite_anterior)) {
						maior_faixa = no.getLimite() - limite_anterior;
						no_aux = no;
					}
					limite_anterior = no.getLimite();

				}

				List<KeyValue> elems = new Dispatcher(no_aux.getIp(), no_aux.getPorta())
						.getRange((no_aux.getLimite() - (maior_faixa)), no_aux.getLimite() - (maior_faixa / 2));

				System.out.println("------------------");
				for (KeyValue keyvalue : elems) {

					System.out.println("elem: " + keyvalue.key);
					valores.put(keyvalue.key, keyvalue);
					Dispatcher d = new Dispatcher(no_aux.getIp(), no_aux.getPorta());
					d.remove(keyvalue.key);
					d.fecharConexao();
				}

				conn.createNode(DIR + "/node" + id,
						(id + " localhost " + (6500 + id) + " " + (no_aux.getLimite() - (maior_faixa / 2)) + "")
								.getBytes());

			}

			List<String> zNodes = conn.getZk().getChildren(DIR, true);

			for (String zNode : zNodes) {
				System.out.print("nó: " + zNode + "  ---  ");
				byte[] data = conn.getZk().getData(DIR + "/" + zNode, true,
						conn.getZk().exists(DIR + "/" + zNode, true));
				linha = new String(data);

				System.out.println(linha);
				if (Integer.parseInt(linha.split(" ")[0]) == id) {
					PORT = Integer.parseInt(linha.split(" ")[2]);
					faixa = Long.parseLong(linha.split(" ")[3]);
				}

				nodes.add(new No(Integer.parseInt(linha.split(" ")[0]), linha.split(" ")[1],
						Integer.parseInt(linha.split(" ")[2]), Long.parseLong(linha.split(" ")[3])));

			}
			/*
			 * 
			 * bf = new BufferedReader(new FileReader(new File("conf"))); linha
			 * = ""; while ((linha = bf.readLine()) != null) { if
			 * (Integer.parseInt(linha.split(" ")[0]) == id) { PORT =
			 * Integer.parseInt(linha.split(" ")[2]); faixa =
			 * Integer.parseInt(linha.split(" ")[3]);
			 * 
			 * }
			 * 
			 * nodes.add(new No(Integer.parseInt(linha.split(" ")[0]),
			 * linha.split(" ")[1], Integer.parseInt(linha.split(" ")[2]),
			 * Long.parseLong(linha.split(" ")[3])));
			 * 
			 * }
			 */

			System.out.println(id + " - " + PORT + ": " + faixa);

			handler = new ServerHandler(faixa, nodes, valores);
			processor = new LameDB.Processor(handler);

			Runnable simple = new Runnable() {
				public void run() {
					simple(processor);
				}
			};
			Runnable secure = new Runnable() {
				public void run() {
					secure(processor);
				}
			};

			new Thread(simple).start();
			// new Thread(secure).start();
		} catch (Exception x) {
			x.printStackTrace();
		}
	}

	public static void simple(LameDB.Processor processor) {
		try {
			TServerTransport serverTransport = new TServerSocket(PORT);
			// TServer server = new TSimpleServer(new
			// Args(serverTransport).processor(processor));

			System.out.println("Starting the simple server...");
			// Use this for a multithreaded server
			TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

			server.serve();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void secure(LameDB.Processor processor) {
		try {
			/*
			 * Use TSSLTransportParameters to setup the required SSL parameters.
			 * In this example we are setting the keystore and the keystore
			 * password. Other things like algorithms, cipher suites, client
			 * auth etc can be set.
			 */
			TSSLTransportParameters params = new TSSLTransportParameters();
			// The Keystore contains the private key
			params.setKeyStore("../../lib/java/test/.keystore", "thrift", null, null);

			/*
			 * Use any of the TSSLTransportFactory to get a server transport
			 * with the appropriate SSL configuration. You can use the default
			 * settings if properties are set in the command line. Ex:
			 * -Djavax.net.ssl.keyStore=.keystore and
			 * -Djavax.net.ssl.keyStorePassword=thrift
			 * 
			 * Note: You need not explicitly call open(). The underlying server
			 * socket is bound on return from the factory class.
			 */
			TServerTransport serverTransport = TSSLTransportFactory.getServerSocket(9091, 0, null, params);
			TServer server = new TSimpleServer(new Args(serverTransport).processor(processor));

			// Use this for a multi threaded server
			// TServer server = new TThreadPoolServer(new
			// TThreadPoolServer.Args(serverTransport).processor(processor));

			System.out.println("Starting the secure server...");
			server.serve();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}