package compatibilityUtils;

//import com.sun.jmx.snmp.InetAddressAcl;

public class MaximalInterfaceCompatibility {
	
	private String IRSignature;
	private String ISSignature;
	public MaximalInterfaceCompatibility(String iRSignature, String iSSignature) {
		super();
		IRSignature = iRSignature;
		ISSignature = iSSignature;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((IRSignature == null) ? 0 : IRSignature.hashCode());
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
		MaximalInterfaceCompatibility other = (MaximalInterfaceCompatibility) obj;
		if (IRSignature == null || ISSignature == null) {
			if (other.IRSignature != null  || other.ISSignature == null)
				return false;
		} 
		else if (!IRSignature.equals(other.IRSignature) || !ISSignature.equals(other.ISSignature))
			return false;
		return true;
	}
	public String getIRSignature() {
		return IRSignature;
	}
	public void setIRSignature(String iRSignature) {
		IRSignature = iRSignature;
	}
	public String getISSignature() {
		return ISSignature;
	}
	public void setISSignature(String iSSignature) {
		ISSignature = iSSignature;
	}
	
	
	
	
	}
