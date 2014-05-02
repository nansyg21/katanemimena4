import java.io.Serializable;


public class Communication implements Serializable {
	private String property;
	private String message;
	
	public Communication(){
		
	}
	public Communication(String _message, String _property ){
		
		property = _property;
		message = _message;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	

}
