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
package ca.classicdiy.j2modlite.io;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import ca.classicdiy.j2modlite.Modbus;
import ca.classicdiy.j2modlite.ModbusIOException;
import ca.classicdiy.j2modlite.msg.ModbusMessage;
import ca.classicdiy.j2modlite.msg.ModbusRequest;
import ca.classicdiy.j2modlite.msg.ModbusResponse;
import ca.classicdiy.j2modlite.net.TCPMasterConnection;
import ca.classicdiy.j2modlite.util.ModbusUtil;

/**
 * Class that implements the Modbus transport flavor.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 * 
 * @version 110410-jfh Cleaned up unused variables. Stopped spewing out error
 *          messages.
 */
public class ModbusTCPTransport implements ca.classicdiy.j2modlite.io.ModbusTransport {

	// instance attributes
	private DataInputStream m_Input; // input stream
	private DataOutputStream m_Output; // output stream
	private ca.classicdiy.j2modlite.io.BytesInputStream m_ByteIn;
	private ca.classicdiy.j2modlite.io.BytesOutputStream m_ByteOut; // write frames
	private int m_Timeout = Modbus.DEFAULT_TIMEOUT;
	private Socket m_Socket = null;
	private	TCPMasterConnection m_Master = null;
	private boolean headless = false; // Some TCP implementations are.

	/**
	 * Sets the <tt>Socket</tt> used for message transport and prepares the
	 * streams used for the actual I/O.
	 * 
	 * @param socket
	 *            the <tt>Socket</tt> used for message transport.
	 * @throws IOException
	 *             if an I/O related error occurs.
	 */
	public void setSocket(Socket socket) throws IOException {
		if (m_Socket != null) {
			m_Socket.close();
			m_Socket = null;
		}
		m_Socket = socket;
		setTimeout(m_Timeout);

		prepareStreams(socket);
	}// setSocket

	public void setHeadless() {
		headless = true;
	}

	public void setTimeout(int time) {
		m_Timeout = time;

		if (m_Socket != null) {
			try {
				m_Socket.setSoTimeout(time);
			} catch (SocketException e) {
				// Not sure what to do.
				return;
			}
		}
	}

	public void close() throws IOException {
		m_Input.close();
		m_Output.close();
		m_Socket.close();
	}// close

	  public ca.classicdiy.j2modlite.io.ModbusTransaction createTransaction() {
		  if (m_Master == null) {
			  m_Master = new TCPMasterConnection(m_Socket.getInetAddress());
			  m_Master.setPort(m_Socket.getPort());
			  m_Master.setModbusTransport(this);
		  }
		  ca.classicdiy.j2modlite.io.ModbusTCPTransaction trans = new ca.classicdiy.j2modlite.io.ModbusTCPTransaction(m_Master);
		  return trans;
	  }

	public void writeMessage(ModbusMessage msg) throws ModbusIOException {
		try {
			byte message[] = msg.getMessage();

			m_ByteOut.reset();
			if (!headless) {
				m_ByteOut.writeShort(msg.getTransactionID());
				m_ByteOut.writeShort(msg.getProtocolID());
				m_ByteOut
						.writeShort((message != null ? message.length : 0) + 2);
			}
			m_ByteOut.writeByte(msg.getUnitID());
			m_ByteOut.writeByte(msg.getFunctionCode());
			if (message != null && message.length > 0)
				m_ByteOut.write(message);

			m_Output.write(m_ByteOut.toByteArray());
			m_Output.flush();
			if (Modbus.debug)
                Log.d(getClass().getName(), "Sent: "
						+ ModbusUtil.toHex(m_ByteOut.toByteArray()));
			// write more sophisticated exception handling
		} catch (SocketException ex) {
			if (m_Master != null && !m_Master.isConnected()) {
				try {
					m_Master.connect();
				} catch (Exception e) {
					// Do nothing.
				}
			}
			ex.printStackTrace();
			throw new ModbusIOException("I/O exception - failed to write.");
		} catch (Exception ex) {
			throw new ModbusIOException("I/O exception - failed to write.");
		}
	}

	/**
	 * readRequest -- Read a Modbus TCP encoded request. The packet has a 6 byte
	 * header containing the protocol, transaction ID and length.
	 * 
	 * @returns Modbus response for message
	 * @throws ModbusIOException
	 */
	public ModbusRequest readRequest() throws ModbusIOException {

	try {
			ModbusRequest req = null;
			m_ByteIn.reset();

			synchronized (m_ByteIn) {
				byte[] buffer = m_ByteIn.getBuffer();

				if (!headless) {
					if (m_Input.read(buffer, 0, 6) == -1)
						throw new EOFException(
								"Premature end of stream (Header truncated).");

					int transaction = ModbusUtil.registerToShort(buffer, 0);
					int protocol = ModbusUtil.registerToShort(buffer, 2);
					int count = ModbusUtil.registerToShort(buffer, 4);

					if (m_Input.read(buffer, 6, count) == -1)
						throw new ModbusIOException(
								"Premature end of stream (Message truncated).");

					if (Modbus.debug)
                        Log.d(getClass().getName(), "Read: "
								+ ModbusUtil.toHex(buffer, 0, count + 6));
					
					m_ByteIn.reset(buffer, (6 + count));
					m_ByteIn.skip(6);

					int unit = m_ByteIn.readByte();
					int functionCode = m_ByteIn.readUnsignedByte();

					m_ByteIn.reset();
					req = ModbusRequest.createModbusRequest(functionCode);
					req.setUnitID(unit);
					req.setHeadless(false);

					req.setTransactionID(transaction);
					req.setProtocolID(protocol);
					req.setDataLength(count);

					req.readFrom(m_ByteIn);
				} else {
					
					/*
					 * This is a headless request.
					 */
					int unit = m_Input.readByte();
					int function = m_Input.readByte();

					req = ModbusRequest.createModbusRequest(function);
					req.setUnitID(unit);
					req.setHeadless(true);

					req.readData(m_Input);

					/*
					 * Discard the CRC. This is a TCP/IP connection, which has
					 * proper error correction and recovery.
					 */
					m_Input.readShort();
					if (Modbus.debug)
                        Log.d(getClass().getName(), "Read: "	+ req.getHexMessage());
				}
			}
			return req;
		} catch (EOFException eoex) {
			throw new ModbusIOException("End of File", true);
		} catch (SocketTimeoutException x) {
			throw new ModbusIOException("Timeout reading request");
		} catch (SocketException sockex) {
			throw new ModbusIOException("Socket Exception", true);
		} catch (Exception ex) {
			throw new ModbusIOException("I/O exception - failed to read.");
		}
	}

	public ModbusResponse readResponse() throws ModbusIOException {

		try {

			ModbusResponse response = null;

			synchronized (m_ByteIn) {
				// use same buffer
				byte[] buffer = m_ByteIn.getBuffer();
				if (Modbus.debug)
                    Log.d(getClass().getName(), "Read: " +
							ModbusUtil.toHex(buffer, 0, m_ByteIn.count));
				
				if (!headless) {
					/*
					 * All Modbus TCP transactions start with 6 bytes. Get them.
					 */
					if (m_Input.read(buffer, 0, 6) == -1)
						throw new ModbusIOException(
								"Premature end of stream (Header truncated).");

					/*
					 * The transaction ID is the first word (offset 0) in the
					 * data that was just read. It will be echoed back to the
					 * requester.
					 * 
					 * The protocol ID is the second word (offset 2) in the
					 * data. It should always be 0, but I don't check.
					 * 
					 * The length of the payload is the third word (offset 4) in
					 * the data that was just read. That's what I need in order
					 * to read the rest of the response.
					 */
					int transaction = ModbusUtil.registerToShort(buffer, 0);
					int protocol = ModbusUtil.registerToShort(buffer, 2);
					int count = ModbusUtil.registerToShort(buffer, 4);

					if (m_Input.read(buffer, 6, count) == -1)
						throw new ModbusIOException(
								"Premature end of stream (Message truncated).");

					m_ByteIn.reset(buffer, (6 + count));

					m_ByteIn.reset();
					m_ByteIn.skip(7);
					int function = m_ByteIn.readUnsignedByte();
					response = ModbusResponse.createModbusResponse(function);

					/*
					 * Rewind the input buffer, then read the data into the
					 * response.
					 */
					m_ByteIn.reset();
					response.readFrom(m_ByteIn);

					response.setTransactionID(transaction);
					response.setProtocolID(protocol);
				} else {
					/*
					 * This is a headless response. It has the same format as a
					 * RTU over Serial response.
					 */
					int unit = m_Input.readByte();
					int function = m_Input.readByte();

					response = ModbusResponse.createModbusResponse(function);
					response.setUnitID(unit);
					response.setHeadless();
					response.readData(m_Input);

					/*
					 * Now discard the CRC. Which hopefully wasn't needed
					 * because this is a TCP transport.
					 */
					m_Input.readShort();
				}
			}
			return response;
		} catch (SocketTimeoutException ex) {
			throw new ModbusIOException("Timeout reading response");
		} catch (Exception ex) {
			throw new ModbusIOException("I/O exception - failed to read.");
		}
	}

	/**
	 * Prepares the input and output streams of this <tt>ModbusTCPTransport</tt>
	 * instance based on the given socket.
	 * 
	 * @param socket
	 *            the socket used for communications.
	 * @throws IOException
	 *             if an I/O related error occurs.
	 */
	private void prepareStreams(Socket socket) throws IOException {

		/*
		 * Close any open streams if I'm being called because a new socket was
		 * set to handle this transport.
		 */
		try {
			if (m_Input != null)
				m_Input.close();

			if (m_Output != null)
				m_Output.close();
		} catch (IOException x) {
			// Do nothing.
		}

		m_Input = new DataInputStream(new BufferedInputStream(
				socket.getInputStream()));

		m_Output = new DataOutputStream(new BufferedOutputStream(
				socket.getOutputStream()));

		/*
		 * Modbus/TCP adds a header which must be accounted for.
		 */
		m_ByteIn = new BytesInputStream(Modbus.MAX_MESSAGE_LENGTH+6);

		m_ByteOut = new BytesOutputStream(Modbus.MAX_MESSAGE_LENGTH+6);
	}

	/**
	 * Constructs a new <tt>ModbusTransport</tt> instance, for a given
	 * <tt>Socket</tt>.
	 * <p>
	 * 
	 * @param socket
	 *            the <tt>Socket</tt> used for message transport.
	 */
	public ModbusTCPTransport(Socket socket) {
		try {
			setSocket(socket);
			socket.setSoTimeout(m_Timeout);
		} catch (IOException ex) {
			if (Modbus.debug)
                Log.w(getClass().getName(), "ModbusTCPTransport::Socket invalid.");

			throw new IllegalStateException("Socket invalid.");
		}
	}
}