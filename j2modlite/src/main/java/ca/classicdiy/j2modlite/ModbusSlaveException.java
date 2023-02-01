//License
/***
 * Java Modbus Library (jamod)
 * Copyright (c) 2002-2004, jamod development team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the author nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDER AND CONTRIBUTORS ``AS
 * IS'' AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO,
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 ***/
package ca.classicdiy.j2modlite;

/**
 * Class that implements a <tt>ModbusSlaveException</tt>. Instances of this
 * exception are thrown when the slave returns a Modbus exception.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public class ModbusSlaveException extends ModbusException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Instance type attribute
	 */
	private int m_Type = -1;

	/**
	 * <p>
	 * Constructs a new <tt>ModbusSlaveException</tt> instance with the given
	 * type.
	 * 
	 * <p>
	 * Types are defined according to the protocol specification in
	 * <tt>net.wimpi.modbus.Modbus</tt>.
	 * 
	 * @param TYPE
	 *            the type of exception that occured.
	 * 
	 * @see net.wimpi.modbus.Modbus
	 */
	public ModbusSlaveException(int TYPE) {
		super();

		m_Type = TYPE;
	}

	/**
	 * <p>
	 * Returns the type of this <tt>ModbusSlaveException</tt>. <br>
	 * Types are defined according to the protocol specification in
	 * <tt>net.wimpi.modbus.Modbus</tt>.
	 * 
	 * @return the type of this <tt>ModbusSlaveException</tt>.
	 * 
	 * @see net.wimpi.modbus.Modbus
	 */
	public int getType() {
		return m_Type;
	}

	/**
	 * <p>
	 * Tests if this <tt>ModbusSlaveException</tt> is of a given type.
	 * 
	 * <p>
	 * Types are defined according to the protocol specification in
	 * <tt>net.wimpi.modbus.Modbus</tt>.
	 * 
	 * @param TYPE
	 *            the type to test this <tt>ModbusSlaveException</tt> type
	 *            against.
	 * 
	 * @return true if this <tt>ModbusSlaveException</tt> is of the given type,
	 *         false otherwise.
	 * 
	 * @see net.wimpi.modbus.Modbus
	 */
	public boolean isType(int TYPE) {
		return (TYPE == m_Type);
	}

	/**
	 * Get the exception type message associated with this exception.
	 * 
	 * @returns a String indicating the type of slave exception.
	 */
	public String getMessage() {
		return getMessage(m_Type);
	}

	/**
	 * Get the exception type message associated with the given exception
	 * number.
	 * 
	 * @param type
	 *            Numerical value of the Modbus exception.
	 * @return a String indicating the type of slave exception.
	 */
	public static String getMessage(int type) {
		switch (type) {
		case 1:
			return "Illegal Function";
		case 2:
			return "Illegal Data Address";
		case 3:
			return "Illegal Data Value";
		case 4:
			return "Slave Device Failure";
		case 5:
			return "Acknowledge";
		case 6:
			return "Slave Device Busy";
		case 8:
			return "Memory Parity Error";
		}
		return "Error Code = " + type;
	}
}
