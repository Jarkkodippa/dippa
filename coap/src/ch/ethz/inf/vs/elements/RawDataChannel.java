
package ch.ethz.inf.vs.elements;

import javax.xml.ws.Endpoint;
import javax.xml.ws.Response;

import org.omg.CORBA.Request;

/**
 * This is the interface needed between a CoAP stack and a connector. The
 * connector forwards raw data to the method receiveData() and the CoAP stack
 * forwards messages to the corresponding method sendX(). {@link Endpoint} uses
 * this interface to connect {@link CoapStack} to {@link UDPConnector}.
 */
public interface RawDataChannel {

	/**
	 * Forwards the specified data to the stack. First, they must be parsed to a
	 * {@link Request}, {@link Response} or {@link EmptyMessage}. Second, the
	 * matcher finds the corresponding exchange and finally, the stack will
	 * process the message.
	 * 
	 * @param raw
	 *            the raw data
	 */
	public void receiveData(RawData raw);

}
