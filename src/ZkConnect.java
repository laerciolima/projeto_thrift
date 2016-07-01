
import java.util.Date;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.KeeperState;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;

public class ZkConnect {
	private ZooKeeper zk;
	private CountDownLatch connSignal = new CountDownLatch(0);

	public ZkConnect(String host, int port) {
		try {
			zk = new ZooKeeper(host, port, new Watcher() {
				public void process(WatchedEvent event) {
					if (event.getState() == KeeperState.SyncConnected) {
						connSignal.countDown();
					}
				}
			});
			connSignal.await();
		} catch (Exception e) {
			System.out.println("Erro ao inicar conexao com zookeeper");
		}

	}

	public void close() throws InterruptedException {
		zk.close();
	}

	public void createNode(String path, byte[] data) throws Exception {
		try {
			zk.create(path, data, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
		} catch (Exception e) {
		}

	}

	public void updateNode(String path, byte[] data) throws Exception {
		zk.setData(path, data, zk.exists(path, true).getVersion());
	}

	public void deleteNode(String path) throws Exception {
		zk.delete(path, zk.exists(path, true).getVersion());
	}

	public ZooKeeper getZk() {
		return zk;
	}

	public TreeSet<No> getNodes(String dir) {
		TreeSet<No> nodes = new TreeSet<>();

		List<String> zNodes;
		try {
			zNodes = zk.getChildren(dir, true);

			String linha = "";
			for (String zNode : zNodes) {
				byte[] data = zk.getData(dir + "/" + zNode, true, zk.exists(dir + "/" + zNode, true));
				linha = new String(data);

				nodes.add(new No(Integer.parseInt(linha.split(" ")[0]), linha.split(" ")[1],
						Integer.parseInt(linha.split(" ")[2]), Long.parseLong(linha.split(" ")[3])));
			}

		} catch (KeeperException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return nodes;
	}

	

}