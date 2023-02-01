/**
 * 
 */
package ca.classicdiy.j2modlite.msg;

import ca.classicdiy.j2modlite.Modbus;

/**
 * @author Julie
 *
 * @version @version@ (@date@)
 */
public class IllegalAddressExceptionResponse extends ExceptionResponse {

	/**
	 * 
	 */
	public void setFunctionCode(int fc) {
		super.setFunctionCode(fc | Modbus.EXCEPTION_OFFSET);
	}
	
	/**
	 * 
	 */
	public IllegalAddressExceptionResponse() {
		super(0, Modbus.ILLEGAL_ADDRESS_EXCEPTION);		
	}
	
	public IllegalAddressExceptionResponse(int function) {
		super(function, Modbus.ILLEGAL_ADDRESS_EXCEPTION);
	}
}
