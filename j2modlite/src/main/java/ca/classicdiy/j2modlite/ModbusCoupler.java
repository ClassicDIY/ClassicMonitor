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

import ca.classicdiy.j2modlite.procimg.DefaultProcessImageFactory;
import ca.classicdiy.j2modlite.procimg.ProcessImage;
import ca.classicdiy.j2modlite.procimg.ProcessImageFactory;

/**
 * Class implemented following a Singleton pattern, to couple the slave side
 * with a master side or with a device.
 * 
 * <p>
 * At the moment it only provides a reference to the OO model of the process
 * image.
 * 
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public class ModbusCoupler {

	// class attributes
	private static ModbusCoupler c_Self; // Singleton reference

	// instance attributes
	private ProcessImage m_ProcessImage;
	private int m_UnitID = Modbus.DEFAULT_UNIT_ID;
	private boolean m_Master = true;
	private ProcessImageFactory m_PIFactory;

	/**
	 * A private constructor which creates a default process image.
	 */
	private ModbusCoupler() {
		m_PIFactory = new DefaultProcessImageFactory();
	}

	/**
	 * Private constructor to prevent multiple instantiation.
	 * 
	 * @param procimg
	 *            a <tt>ProcessImage</tt>.
	 */
	private ModbusCoupler(ProcessImage procimg) {
		setProcessImage(procimg);
		c_Self = this;
	}

	/**
	 * Returns the actual <tt>ProcessImageFactory</tt> instance.
	 * 
	 * @return a <tt>ProcessImageFactory</tt> instance.
	 */
	public ProcessImageFactory getProcessImageFactory() {
		return m_PIFactory;
	}

	/**
	 * Sets the <tt>ProcessImageFactory</tt> instance.
	 * 
	 * @param factory
	 *            the instance to be used for creating process image instances.
	 */
	public void setProcessImageFactory(ProcessImageFactory factory) {
		m_PIFactory = factory;
	}

	/**
	 * Returns a reference to the <tt>ProcessImage</tt> of this
	 * <tt>ModbusCoupler</tt>.
	 * 
	 * @return the <tt>ProcessImage</tt>.
	 */
	public synchronized ProcessImage getProcessImage() {
		return m_ProcessImage;
	}

	/**
	 * Sets the reference to the <tt>ProcessImage</tt> of this
	 * <tt>ModbusCoupler</tt>.
	 * 
	 * @param procimg
	 *            the <tt>ProcessImage</tt> to be set.
	 */
	public synchronized void setProcessImage(ProcessImage procimg) {
		m_ProcessImage = procimg;
	}

	/**
	 * Returns the identifier of this unit. This identifier is required to be
	 * set for serial protocol slave implementations.
	 * 
	 * @return the unit identifier as <tt>int</tt>.
	 */
	public int getUnitID() {
		return m_UnitID;
	}

	/**
	 * Sets the identifier of this unit, which is needed to be determined in a
	 * serial network.
	 * 
	 * @param id
	 *            the new unit identifier as <tt>int</tt>.
	 */
	public void setUnitID(int id) {
		m_UnitID = id;
	}

	/**
	 * Tests if this instance is a master device.
	 * 
	 * @return true if master, false otherwise.
	 */
	public boolean isMaster() {
		return m_Master;
	}

	/**
	 * Tests if this instance is not a master device.
	 * 
	 * @return true if slave, false otherwise.
	 */
	public boolean isSlave() {
		return ! m_Master;
	}

	/**
	 * Sets this instance to be or not to be a master device.
	 * 
	 * @param master
	 *            true if master device, false otherwise.
	 */
	public void setMaster(boolean master) {
		m_Master = master;
	}
	
	public static final boolean isInitialized() {
		return c_Self != null;
	}

	/**
	 * Returns a reference to the singleton instance.
	 * 
	 * @return the <tt>ModbusCoupler</tt> instance reference.
	 */
	public static synchronized final ModbusCoupler getReference() {
		if (c_Self == null)
			return (c_Self = new ModbusCoupler());
		else
			return c_Self;
	}
}
