package net.vojir.droolsserver.drools;


public class DrlAR {
	private String id = "";
	private String bestId = "";
	private int antecedentLength = -1;
	private double confidenceValue = -1;
	private double supportValue = -1;
	private boolean checkedOk=false;

    private String model;
	
	public DrlAR(){}
	
	public DrlAR(String id){
		setId(id);
	}
	
	public DrlAR(String model, String id,int antecedentLength, double confidenceValue, double supportValue){
        this.model = model;
		setId(id);
		setAntecedentLength(antecedentLength);
		setConfidenceValue(confidenceValue);
		setSupportValue(supportValue);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getBestId() {
		return bestId;
	}

	public void setBestId(String bestId) {
		this.bestId = bestId;
	}
	
	public int getAntecedentLength() {
		return antecedentLength;
	}

	public void setAntecedentLength(int antecedentLength) {
		this.antecedentLength = antecedentLength;
	}

	public double getConfidenceValue() {
		return confidenceValue;
	}

	public void setConfidenceValue(double confidenceValue2) {
		this.confidenceValue = confidenceValue2;
	}

	public double getSupportValue() {
		return supportValue;
	}

	public void setSupportValue(double supportValue2) {
		this.supportValue = supportValue2;
	}

	public boolean isCheckedOk() {
		return checkedOk;
	}

	public void setCheckedOk(boolean checkedOk) {
		this.checkedOk = checkedOk;
	}
	
	public void updateFromAR(DrlAR ar){
		this.setId(ar.getId());
		this.setAntecedentLength(ar.getAntecedentLength());
		this.setConfidenceValue(ar.getConfidenceValue());
		this.setSupportValue(ar.getSupportValue());
		this.setCheckedOk(false);
		this.setBestId(ar.getId());
	}


    @Override
    public boolean equals( Object o ) {
        if ( this == o ) return true;
        if ( o == null || getClass() != o.getClass() ) return false;

        DrlAR drlAR = (DrlAR) o;

        if ( !model.equals( drlAR.model ) ) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return model.hashCode();
    }

    @Override
    public String toString() {
        return "DrlAR{" +
               "id='" + id + '\'' +
               ", bestId='" + bestId + '\'' +
               ", antecedentLength=" + antecedentLength +
               ", confidenceValue=" + confidenceValue +
               ", supportValue=" + supportValue +
               ", checkedOk=" + checkedOk +
               '}';
    }
}
