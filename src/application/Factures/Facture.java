package application.Factures;

public class Facture {
 
	private String desA;
    private int puA;
    private int qteA;
    private int mtA;	
	  
    Facture() {
    	super();
    }

	public Facture(String desA, int puA, int qteA, int mtA) {
		super();
		this.desA = desA;
		this.puA = puA;
		this.qteA = qteA;
		this.mtA = mtA;
	}

	public String getDesA() {
		return desA;
	}

	public void setDesA(String desA) {
		this.desA = desA;
	}

	public int getPuA() {
		return puA;
	}

	public void setPuA(int puA) {
		this.puA = puA;
	}

	public int getQteA() {
		return qteA;
	}

	public void setQteA(int qteA) {
		this.qteA = qteA;
	}

	public int getMtA() {
		return mtA;
	}

	public void setMtA(int mtA) {
		this.mtA = mtA;
	} 
	  
	  
}
