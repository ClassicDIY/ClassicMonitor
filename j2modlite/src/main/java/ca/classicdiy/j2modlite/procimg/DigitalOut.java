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
package ca.classicdiy.j2modlite.procimg;

/**
 * Interface defining a digital output.
 * <p>
 * In Modbus terms this represents a
 * coil, which is read-write from slave and
 * master or device side.<br>
 * Therefor implementations have to be carefully
 * designed for concurrency.
 *
 * @author Dieter Wimberger
 * @version 1.2rc1 (09/11/2004)
 */
public interface DigitalOut {

  /**
   * Tests if this <tt>DigitalOut</tt> is set.
   * <p>
   *
   * @return true if set, false otherwise.
   */
  public boolean isSet();

  /**
   * Sets the state of this <tt>DigitalOut</tt>.
   * <p>
   *
   * @param b true if to be set, false otherwise.
   */
  public void set(boolean b);

}//DigitalOut
