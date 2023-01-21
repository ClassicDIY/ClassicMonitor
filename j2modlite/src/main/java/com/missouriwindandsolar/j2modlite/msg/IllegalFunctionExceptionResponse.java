/**
 * 
 */
package com.missouriwindandsolar.j2modlite.msg;

import com.missouriwindandsolar.j2modlite.Modbus;

/**
 * @author jfhaugh
 * 
 * @version @version@ (@date@)
 */
public class IllegalFunctionExceptionResponse extends ExceptionResponse {

	/**
	 * 
	 */
	public void setFunctionCode(int fc) {
		super.setFunctionCode(fc | Modbus.EXCEPTION_OFFSET);
	}

	/**
	 * 
	 */
	public IllegalFunctionExceptionResponse() {
		super(0, Modbus.ILLEGAL_FUNCTION_EXCEPTION);
	}

	public IllegalFunctionExceptionResponse(int function) {
		super(function | Modbus.EXCEPTION_OFFSET,
				Modbus.ILLEGAL_FUNCTION_EXCEPTION);
	}
}
