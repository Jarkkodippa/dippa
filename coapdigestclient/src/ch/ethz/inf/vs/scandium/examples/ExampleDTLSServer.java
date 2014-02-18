/*******************************************************************************
 * Copyright (c) 2013, Institute for Pervasive Computing, ETH Zurich.
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the Institute nor the names of its contributors
 * may be used to endorse or promote products derived from this software
 * without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE INSTITUTE AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE INSTITUTE OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * 
 * This file is part of Scandium (Sc) Security for Californium.
 ******************************************************************************/

package ch.ethz.inf.vs.scandium.examples;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.logging.Level;

import ch.ethz.inf.vs.elements.Connector;
import ch.ethz.inf.vs.elements.RawData;
import ch.ethz.inf.vs.elements.RawDataChannel;
import ch.ethz.inf.vs.scandium.DTLSConnector;
import ch.ethz.inf.vs.scandium.util.ScProperties;
import ch.ethz.inf.vs.scandium.util.ScandiumLogger;


public class ExampleDTLSServer {

	public static final int DEFAULT_PORT = ScProperties.std.getInt("DEFAULT_PORT");
	
	private Connector dtlsConnector;
	
	public ExampleDTLSServer() {
		dtlsConnector = new DTLSConnector(new InetSocketAddress(DEFAULT_PORT));
		dtlsConnector.setRawDataReceiver(new RawDataChannelImpl(dtlsConnector));
	}
	
	public void start() {
		try {
			dtlsConnector.start();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private class RawDataChannelImpl implements RawDataChannel {
		
		private Connector connector;
		
		public RawDataChannelImpl(Connector con) {
			this.connector = con;
		}

		// @Override
		public void receiveData(final RawData raw) {
			if (raw.getAddress() == null)
				throw new NullPointerException();
			if (raw.getPort() == 0)
				throw new NullPointerException();
			
			System.out.println(new String(raw.getBytes()));
			connector.send(new RawData("ACK".getBytes(), raw.getAddress(), raw.getPort()));
		}
	}
	
	public static void main(String[] args) {
		
		//ScandiumLogger.setLoggerLevel(Level.WARNING);
		
		ExampleDTLSServer server = new ExampleDTLSServer();
		server.start();
	}
}
