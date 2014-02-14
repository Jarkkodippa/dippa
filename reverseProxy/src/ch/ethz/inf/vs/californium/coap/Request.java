package ch.ethz.inf.vs.californium.coap;

import java.net.InetAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import ch.ethz.inf.vs.californium.coap.CoAP.Code;
import ch.ethz.inf.vs.californium.coap.CoAP.Type;
import ch.ethz.inf.vs.californium.network.Endpoint;
import ch.ethz.inf.vs.californium.network.EndpointManager;


/**
 * Request represents a CoAP request and has either the {@link Type} CON or NON
 * and one of the {@link CoAP.Code}s GET, POST, PUT or DELETE. A request must be
 * sent over an {@link Endpoint} to its destination. By default, a request
 * chooses the default endpoint defined in {@link EndpointManager}. The server
 * responds with a {@link Response}. The client can wait for such a response
 * with a synchronous call, for instance:
 * 
 * <pre>
 * Request request = new Request(Code.GET);
 * request.setURI(&quot;coap://example.com:5683/sensors/temperature&quot;);
 * request.send();
 * Response response = request.waitForResponse();
 * </pre>
 * 
 * The client can also send requests asynchronously and define a handler that is
 * invoked when a response arrives. This is in particular useful, when a client
 * wants to observe the target resource and react to notifications. For
 * instance:
 * 
 * <pre>
 * Request request = new Request(Code.GET);
 * request.setURI(&quot;coap://example.com:5683/sensors/temperature&quot;);
 * request.setObserve();
 * 
 * request.addMessageObserver(new MessageObserverAdapter() {
 *   public void responded(Response response) {
 *     if (response.getCode() == ResponseCode.CONTENT) {
 *       System.out.println(&quot;Received &quot; + response.getPayloadString());
 *     } else {
 *       // error handling
 *     }
 *   }
 * });
 * request.send();
 * </pre>
 * 
 * We can also modify the options of a request. For example:
 * 
 * <pre>
 * Request post = new Request(Code.POST);
 * post.setPayload("Plain text");
 * post.getOptions()
 *   .setContentFormat(MediaTypeRegistry.TEXT_PLAIN)
 *   .setAccept(MediaTypeRegistry.TEXT_PLAIN)
 *   .setIfNoneMatch(true);
 * String response = post.send().waitForResponse().getPayloadString();
 * </pre>
 * @see Response
 */
public class Request extends Message {
	
	private final static Logger LOGGER = Logger.getLogger(Request.class.getCanonicalName());
	
	/** The request code. */
	private final CoAP.Code code;
	
	/** Marks this request as multicast request */
	private boolean multicast;
	
	/** The current response for the request. */
	private Response response;
	
	private String scheme;
	
	/** The lock object used to wait for a response. */
	private Object lock;
	
	/**
	 * Instantiates a new request with the specified CoAP code and no (null)
	 * message type.
	 * 
	 * @param code the request code
	 */
	public Request(Code code) {
		super();
		this.code = code;
	}
	
	/**
	 * Instantiates a new request with the specified CoAP code and message type.
	 * 
	 * @param code the request code
	 * @param type the message type
	 */
	public Request(Code code, Type type) {
		super();
		this.code = code;
		super.setType(type);
	}
	
	/**
	 * Gets the request code.
	 *
	 * @return the code
	 */
	public Code getCode() {
		return code;
	}
	
	/**
	 * Gets the scheme.
	 *
	 * @return the scheme
	 */
	public String getScheme() {
		return scheme;
	}
	
	/**
	 * Sets the scheme.
	 *
	 * @param scheme the new scheme
	 */
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}
	
	/**
	 * Tests if this request is a multicast request
	 * 
	 * @return true if this request is a multicast request.
	 */
	public boolean isMulticast() {
		return multicast;
	}

	/**
	 * Defines whether this request is a multicast request or not.
	 * 
	 * @param multicast if this request is a multicast request
	 */
	public void setMulticast(boolean multicast) {
		this.multicast = multicast;
	}
	
	public Request setPayload(String payload) {
		super.setPayload(payload);
		return this;
	}
	
	public Request setPayload(byte[] payload) {
		super.setPayload(payload);
		return this;
	}
	
	/**
	 * This is a convenience method to set the reques's options for host, port
	 * and path with a string of the form
	 * <code>[scheme]://[host]:[port]{/resource}*?{&query}*</code>
	 * 
	 * @param uri the URI defining the target resource
	 * @return this request
	 * @throws IllegalAccessException if the URI is not valid
	 */
	public Request setURI(String uri) {
		try {
			if (!uri.startsWith("coap://") && !uri.startsWith("coaps://"))
				uri = "coap://" + uri;
			return setURI(new URI(uri));
		} catch (URISyntaxException e) {
			LOGGER.log(Level.WARNING, "Failed to set uri "+uri ,e);
			throw new IllegalArgumentException(e);
		}
	}
	
	/**
	 * This is a convenience method to set the request's options for host, port
	 * and path with a URI object.
	 * 
	 * @param uri the URI defining the target resource
	 * @return this request
	 */
	public Request setURI(URI uri) {
		/*
		 * Implementation from old Cf from Dominique Im Obersteg, Daniel Pauli
		 * and Francesco Corazza.
		 */
		String host = uri.getHost();
		// set Uri-Host option if not IP literal
		if (host != null && !host.toLowerCase().matches("(\\[[0-9a-f:]+\\]|[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3})")) {
			getOptions().setURIHost(host);
		}

		try {
			setDestination(InetAddress.getByName(host));
		} catch (UnknownHostException e) {
    		LOGGER.log(Level.WARNING, "Unknown host as destination", e);
    	}

		String scheme = uri.getScheme();
		if (scheme != null) {
			// decide according to URI scheme whether DTLS is enabled for the client
			this.scheme = scheme;
		}
		
		/*
		 * The Uri-Port is only for special cases where it differs from the UDP port.
		 * (Tell me when that happens...)
		 */
		// set uri-port option
		int port = uri.getPort();
		if (port >= 0) {
			getOptions().setURIPort(port);
			setDestinationPort(port);
		} else if (getDestinationPort() == 0) {
			if (scheme.equals(CoAP.COAP_URI_SCHEME))
				setDestinationPort(EndpointManager.DEFAULT_COAP_PORT);
			else if (scheme.equals(CoAP.COAP_SECURE_URI_SCHEME))
				setDestinationPort(EndpointManager.DEFAULT_COAP_SECURE_PORT);
		}

		// set Uri-Path options
		String path = uri.getPath();
		if (path != null && path.length() > 1) {
			getOptions().setURIPath(path);
		}

		// set Uri-Query options
		String query = uri.getQuery();
		if (query != null) {
			getOptions().setURIQuery(query);
		}
		return this;
	}
	
	// TODO: test this method.
	public String getURI() {
		StringBuilder builder = new StringBuilder();
		String scheme = getScheme();
		if (scheme != null) builder.append(scheme).append("://");
		else builder.append("coap://");
		String host = getOptions().getURIHost();
		if (host != null) builder.append(host+":");
		else builder.append("localhost:");
		Integer port = getOptions().getURIPort();
		if (port != null) builder.append(port);
		else builder.append("TODO" /* TODO: local port */);
		String path = getOptions().getURIPathString();
		builder.append("/").append(path);
		// TODO: Query as well?
		return builder.toString();
	}
	
	/**
	 * Sends the request over the default endpoint to its destination and
	 * expects a response back.
	 */
	public Request send() {
		validateBeforeSending();
		if (CoAP.COAP_SECURE_URI_SCHEME.equals(getScheme())) {
			// This is the case when secure coap is supposed to be used
			EndpointManager.getEndpointManager().getDefaultSecureEndpoint().sendRequest(this);
		} else {
			// This is the normal case
			EndpointManager.getEndpointManager().getDefaultEndpoint().sendRequest(this);
		}
		return this;
	}
	
	/**
	 * Sends the request over the specified endpoint to its destination and
	 * expects a response back.
	 * 
	 * @param endpoint the endpoint
	 */
	public Request send(Endpoint endpoint) {
		validateBeforeSending();
		endpoint.sendRequest(this);
		return this;
	}
	
	/**
	 * Validate before sending that there is a destination set.
	 */
	private void validateBeforeSending() {
		if (getDestination() == null)
			throw new NullPointerException("Destination is null");
		if (getDestinationPort() == 0)
			throw new NullPointerException("Destination port is 0");
	}
	
	/**
	 * Sets CoAP's observe option. If the target resource of this request
	 * responds with a success code and also sets the observe option, it will
	 * send more responses in the future whenever the resource's state changes.
	 */
	public Request setObserve() {
		getOptions().setObserve(0);
		return this;
	}
	
	/**
	 * Gets the response or null if none has arrived yet.
	 *
	 * @return the response
	 */
	public Response getResponse() {
		return response;
	}

	/**
	 * Sets the response.
	 * 
	 * @param response
	 *            the new response
	 */
	public void setResponse(Response response) {
		this.response = response;
		
		if (lock != null)
			synchronized (lock) {
				lock.notifyAll();
			}
		// else: we know that nobody is waiting on the lock
		
		for (MessageObserver handler:getMessageObservers())
			handler.responded(response);
	}
	
	/**
	 * Wait for the response. This function blocks until there is a response or
	 * the request has been canceled.
	 * 
	 * @return the response
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public Response waitForResponse() throws InterruptedException {
		return waitForResponse(0);
	}
	
	/**
	 * Wait for the response. This function blocks until there is a response,
	 * the request has been canceled or the specified timeout has expired. A
	 * timeout of 0 is interpreted as infinity. If a response is already here,
	 * this method returns it immediately.
	 * <p>
	 * The calling thread returns if either a response arrives, the request gets
	 * rejected by the server, the request gets canceled or, in case of a
	 * confirmable request, timeouts. In that case, if no response has arrived
	 * yet the return value is null.
	 * <p>
	 * This method also sets the response to null so that succeeding calls will
	 * wait for the next response. Repeatedly calling this method is useful if
	 * the client expects multiple responses, e.g., multiple notifications to an
	 * observe request or multiple responses to a multicast request.
	 * 
	 * @param timeout
	 *            the maximum time to wait in milliseconds.
	 * @return the response (null if timeout occured)
	 * @throws InterruptedException
	 *             the interrupted exception
	 */
	public Response waitForResponse(long timeout) throws InterruptedException {
		long before = System.currentTimeMillis();
		long expired = timeout>0 ? (before + timeout) : 0;
		// Lazy initialization of a lock
		if (lock == null) {
			synchronized (this) {
				if (lock == null)
					lock = new Object();
			}
		}
		// wait for response
		synchronized (lock) {
			while (response == null 
					&& !isCanceled() && !isTimeouted() && !isRejected()) {
				lock.wait(timeout);
				long now = System.currentTimeMillis();
				if (timeout > 0 && expired <= now) {
					Response r = response;
					response = null;
					return r;
				}
			}
			Response r = response;
			response = null;
			return r;
		}
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Furthermore, if the request is canceled, it will wake up all threads that
	 * are currently waiting for a response.
	 */
	@Override
	public void setTimeouted(boolean timeouted) {
		super.setTimeouted(timeouted);
		if (timeouted && lock != null) {
			synchronized (lock) {
				lock.notifyAll();
			}
		}
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * Furthermore, if the request is canceled, it will wake up all threads that
	 * are currently waiting for a response.
	 */
	@Override
	public void setCanceled(boolean canceled) {
		super.setCanceled(canceled);
		if (canceled && lock != null) {
			synchronized (lock) {
				lock.notifyAll();
			}
		}
	}
	
	@Override
	public void setRejected(boolean rejected) {
		super.setRejected(rejected);
		if (rejected  && lock != null) {
			synchronized (lock) {
				lock.notifyAll();
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String payload = getPayloadString();
		if (payload == null) {
			payload = "no payload";
		} else {
			int len = payload.length();
			if (payload.indexOf("\n")!=-1) payload = payload.substring(0, payload.indexOf("\n"));
			if (payload.length() > 24) payload = payload.substring(0,20);
			payload = "\""+payload+"\"";
			if (payload.length() != len+2) payload += ".. " + payload.length() + " bytes";
		}
		return String.format("%s-%-6s MID=%5d, Token=[%s], %s, %s", getType(), getCode(), getMID(), getTokenString(), getOptions(), payload);
	}
	
	////////// Some static factory methods for convenience //////////
	
	/**
	 * Convenience factory method to construct a GET request and equivalent to
	 * <code>new Request(Code.GET);</code>
	 * 
	 * @return a new GET request
	 */
	public static Request newGet() { return new Request(Code.GET); }
	
	/**
	 * Convenience factory method to construct a POST request and equivalent to
	 * <code>new Request(Code.POST);</code>
	 * 
	 * @return a new POST request
	 */
	public static Request newPost() { return new Request(Code.POST); }
	
	/**
	 * Convenience factory method to construct a PUT request and equivalent to
	 * <code>new Request(Code.PUT);</code>
	 * 
	 * @return a new PUT request
	 */
	public static Request newPut() { return new Request(Code.PUT); }
	
	/**
	 * Convenience factory method to construct a DELETE request and equivalent
	 * to <code>new Request(Code.DELETE);</code>
	 * 
	 * @return a new DELETE request
	 */
	public static Request newDelete() { return new Request(Code.DELETE); }

}
