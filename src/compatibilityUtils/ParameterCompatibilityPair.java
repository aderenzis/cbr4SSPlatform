package compatibilityUtils;

public class ParameterCompatibilityPair {
	
	String IRNameParam;
	String IRTypeParam;
	String ISNameParam;
	String ISTypeParam;
	
	
	public ParameterCompatibilityPair(String iRNameParam, String iRTypeParam,
                                      String iSNameParam, String iSTypeParam) {
		super();
		IRNameParam = iRNameParam;
		IRTypeParam = iRTypeParam;
		ISNameParam = iSNameParam;
		ISTypeParam = iSTypeParam;
	}
	public String getIRNameParam() {
		return IRNameParam;
	}
	public void setIRNameParam(String iRNameParam) {
		IRNameParam = iRNameParam;
	}
	public String getIRTypeParam() {
		return IRTypeParam;
	}
	public void setIRTypeParam(String iRTypeParam) {
		IRTypeParam = iRTypeParam;
	}
	public String getISNameParam() {
		return ISNameParam;
	}
	public void setISNameParam(String iSNameParam) {
		ISNameParam = iSNameParam;
	}
	public String getISTypeParam() {
		return ISTypeParam;
	}
	public void setISTypeParam(String iSTypeParam) {
		ISTypeParam = iSTypeParam;
	}
	@Override
	public String toString() {
		return  IRNameParam+":"+IRTypeParam+"-"+ISNameParam+":"+ISTypeParam;
	}
	
	
	
	

}
