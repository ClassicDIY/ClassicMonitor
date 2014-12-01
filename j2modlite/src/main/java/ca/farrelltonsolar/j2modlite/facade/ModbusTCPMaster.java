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
package ca.farrelltonsolar.j2modlite.facade;


import java.net.InetAddress;

import ca.farrelltonsolar.j2modlite.ModbusException;
import ca.farrelltonsolar.j2modlite.io.ModbusTCPTransaction;
import ca.farrelltonsolar.j2modlite.msg.ReadFileTransferRequest;
import ca.farrelltonsolar.j2modlite.msg.ReadFileTransferResponse;
import ca.farrelltonsolar.j2modlite.msg.ReadInputRegistersRequest;
import ca.farrelltonsolar.j2modlite.msg.ReadInputRegistersResponse;
import ca.farrelltonsolar.j2modlite.msg.ReadMultipleRegistersRequest;
import ca.farrelltonsolar.j2modlite.msg.ReadMultipleRegistersResponse;
import ca.farrelltonsolar.j2modlite.msg.WriteMultipleRegistersRequest;
import ca.farrelltonsolar.j2modlite.msg.WriteSingleRegisterRequest;
import ca.farrelltonsolar.j2modlite.net.TCPMasterConnection;
import ca.farrelltonsolar.j2modlite.procimg.Register;

/**
 * Modbus/TCP Master facade.
 *
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public class ModbusTCPMaster {

    private TCPMasterConnection m_Connection;
    private InetAddress m_SlaveAddress;
    private ModbusTCPTransaction m_Transaction;
    private ReadFileTransferRequest m_FileTransferRequest;
    private ReadInputRegistersRequest m_ReadInputRegistersRequest;
    private ReadMultipleRegistersRequest m_ReadMultipleRegistersRequest;
    private WriteSingleRegisterRequest m_WriteSingleRegisterRequest;
    private WriteMultipleRegistersRequest m_WriteMultipleRegistersRequest;
    private boolean m_Reconnecting = false;
    int _retries = 0;

    public ModbusTCPMaster(InetAddress addr, int port, int unitId) {
        m_SlaveAddress = addr;
        m_Connection = new TCPMasterConnection(m_SlaveAddress);
        m_ReadInputRegistersRequest = new ReadInputRegistersRequest();
        m_ReadInputRegistersRequest.setUnitID(unitId);
        m_ReadMultipleRegistersRequest = new ReadMultipleRegistersRequest();
        m_ReadMultipleRegistersRequest.setUnitID(unitId);
        m_FileTransferRequest = new ReadFileTransferRequest();
        m_FileTransferRequest.setUnitID(unitId);
        m_WriteSingleRegisterRequest = new WriteSingleRegisterRequest();
        m_WriteSingleRegisterRequest.setUnitID(unitId);
        m_WriteMultipleRegistersRequest = new WriteMultipleRegistersRequest();
        m_WriteMultipleRegistersRequest.setUnitID(unitId);
        m_Connection.setPort(port);
    }//constructor

    /**
     * Connects this <tt>ModbusTCPMaster</tt> with the slave.
     *
     * @throws Exception if the connection cannot be established.
     */
    public void connect()
            throws Exception {
        if (m_Connection != null && !m_Connection.isConnected()) {
            m_Connection.connect();
            m_Transaction = new ModbusTCPTransaction(m_Connection);
            m_Transaction.setReconnecting(m_Reconnecting);
            m_Transaction.setRetries(_retries);
        }
    }//connect

    /**
     * Disconnects this <tt>ModbusTCPMaster</tt> from the slave.
     */
    public void disconnect() {
        if (m_Connection != null && m_Connection.isConnected()) {
            m_Connection.close();
            m_Transaction = null;
        }
    }//disconnect

    /**
     * Sets the flag that specifies whether to maintain a
     * constant connection or reconnect for every transaction.
     *
     * @param b true if a new connection should be established for each
     *          transaction, false otherwise.
     */
    public void setReconnecting(boolean b) {
        m_Reconnecting = b;
        if (m_Transaction != null) {
            m_Transaction.setReconnecting(b);
        }
    }//setReconnecting

    public void setRetries(int retries) {
        _retries = retries;
    }

    /**
     * Tests if a constant connection is maintained or if a new
     * connection is established for every transaction.
     *
     * @return true if a new connection should be established for each
     * transaction, false otherwise.
     */
    public boolean isReconnecting() {
        return m_Reconnecting;
    }//isReconnecting

    /**
     * Tests if a constant connection is maintained or if a new
     * connection is established for every transaction.
     *
     * @return true if a new connection should be established for each
     * transaction, false otherwise.
     */
    public boolean isConnected() {
        boolean rVal = false;
        if (m_Connection != null) {
            rVal = m_Connection.isConnected();
        }
        return rVal;
    }



    public synchronized ReadInputRegistersResponse readInputRegisters(int ref, int count)
            throws ModbusException {
        m_ReadInputRegistersRequest.setReference(ref);
        m_ReadInputRegistersRequest.setWordCount(count);
        m_Transaction.setRequest(m_ReadInputRegistersRequest);
        m_Transaction.execute();
        return ((ReadInputRegistersResponse) m_Transaction.getResponse());
    }//readInputRegisters

    /**
     * Reads a given number of registers from the slave.
     * <p/>
     * Note that the number of registers returned (i.e. array length)
     * will be according to the number received in the slave response.
     *
     * @param ref   the offset of the register to start reading from.
     * @param count the number of registers to be read.
     * @return a <tt>Register[]</tt> holding the received registers.
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized ReadMultipleRegistersResponse readMultipleRegisters(int ref, int count)
            throws ModbusException {
        m_ReadMultipleRegistersRequest.setReference(ref);
        m_ReadMultipleRegistersRequest.setWordCount(count);
        m_Transaction.setRequest(m_ReadMultipleRegistersRequest);
        m_Transaction.execute();
        return ((ReadMultipleRegistersResponse) m_Transaction.getResponse());
    }//readMultipleRegisters

    public synchronized ReadFileTransferResponse readFileTransfer(int day, int category, int device)
            throws ModbusException {
        m_FileTransferRequest.setCategory(category);
        m_FileTransferRequest.setDayIndex(day);
        m_FileTransferRequest.setDevice(device);
        m_Transaction.setRequest(m_FileTransferRequest);
        m_Transaction.execute();
        return ((ReadFileTransferResponse) m_Transaction.getResponse());
    }
    /**
     * Writes a single register to the slave.
     *
     * @param ref      the offset of the register to be written.
     * @param register a <tt>Register</tt> holding the value of the register
     *                 to be written.
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized void writeSingleRegister(int ref, Register register)
            throws ModbusException {
        m_WriteSingleRegisterRequest.setReference(ref);
        m_WriteSingleRegisterRequest.setRegister(register);
        m_Transaction.setRequest(m_WriteSingleRegisterRequest);
        m_Transaction.execute();
    }//writeSingleRegister

    /**
     * Writes a number of registers to the slave.
     *
     * @param ref       the offset of the register to start writing to.
     * @param registers a <tt>Register[]</tt> holding the values of
     *                  the registers to be written.
     * @throws ModbusException if an I/O error, a slave exception or
     *                         a transaction error occurs.
     */
    public synchronized void writeMultipleRegisters(int ref, Register[] registers)
            throws ModbusException {
        m_WriteMultipleRegistersRequest.setReference(ref);
        m_WriteMultipleRegistersRequest.setRegisters(registers);
        m_Transaction.setRequest(m_WriteMultipleRegistersRequest);
        m_Transaction.execute();
    }//writeMultipleRegisters

}//class ModbusTCPMaster
