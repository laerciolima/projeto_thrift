
public class No implements Comparable<No>{
	
	private int id;
	private String ip;
	private int porta;
	private long limite;
	
	
	public No(int id, String ip, int porta, long limite) {
		super();
		this.id = id;
		this.ip = ip;
		this.porta = porta;
		this.limite = limite;
	}


	@Override
	public int compareTo(No o) {
		if(this.limite > o.limite)
			return 1;
		else{
			return -1;
		}
		
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getIp() {
		return ip;
	}


	public void setIp(String ip) {
		this.ip = ip;
	}


	public int getPorta() {
		return porta;
	}


	public void setPorta(int porta) {
		this.porta = porta;
	}


	public long getLimite() {
		return limite;
	}


	public void setLimite(long limite) {
		this.limite = limite;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		No other = (No) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
	
	
	

}
