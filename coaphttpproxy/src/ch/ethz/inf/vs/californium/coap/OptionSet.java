package ch.ethz.inf.vs.californium.coap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import ch.ethz.inf.vs.californium.Utils;

/**
 * OptionSet is a collection of all options of a request or a response.
 * OptionSet provides methods to add, remove and modify all options defined in
 * the CoAP, blockwise CoAP, observing CoAP and supports arbitrary defined
 * options.
 * <p>
 * Noteice that this class is not entirely thread-safe: hasObserve => (int) getObserve()
 */
public class OptionSet {

	// TODO: Documentation for all 80 getter/setter
	
	/*
	 * Options defined by the CoAP protocol
	 */
	private List<byte[]> if_match_list;
	private String       uri_host;
	private List<byte[]> etag_list;
	private boolean      if_none_match; // true if option is set
	private Integer      uri_port; // null if no port is explicitly defined
	private List<String> location_path_list;
	private List<String> uri_path_list;
	private Integer      content_format;
	private Long         max_age; // (0-4 bytes)
	private List<String> uri_query_list;
	private Integer      accept;
	private List<String> location_query_list;
	private String       proxy_uri;
	private String       proxy_scheme;
	private BlockOption  block1;
	private BlockOption  block2;
	private Integer      observe;
	
	// Arbitrary options
	private List<Option> others;
	
	// TODO: When receiving, uri_host/port should be those from the sender 
	/*
	 * Once a list is touched and constructed it must never become null again.
	 * Non-lists can be null though.
	 */
	public OptionSet() {
		if_match_list       = null; // new LinkedList<byte[]>();
		uri_host            = null; // from sender
		etag_list           = null; // new LinkedList<byte[]>();
		if_none_match       = false;
		uri_port            = null; // from sender
		location_path_list  = null; // new LinkedList<String>();
		uri_path_list       = null; // new LinkedList<String>();
		content_format      = null;
		max_age             = null;
		uri_query_list      = null; // new LinkedList<String>();
		accept              = null;
		location_query_list = null; // new LinkedList<String>();
		proxy_uri           = null;
		proxy_scheme        = null;
		block1              = null;
		block2              = null;
		observe             = null;
		
		others              = null; // new LinkedList<>();
	}
	
	public void clear() {
		if (if_match_list != null)
			if_match_list.clear();
		uri_host = null;
		if (etag_list != null)
			etag_list.clear();
		if_none_match = false;
		uri_port = null;
		if (location_path_list != null)
			location_path_list.clear();
		if (uri_path_list != null)
			uri_path_list.clear();
		content_format = null;
		max_age = null;
		if (uri_query_list != null)
			uri_query_list.clear();
		accept = null;
		if (location_query_list != null)
			location_path_list.clear();
		proxy_uri = null;
		proxy_scheme = null;
		block1 = null;
		block2 = null;
		observe = null;
		if (others != null)
			others.clear();
	}
	
	/**
	 * Instantiates a new option set equal to the specified one by deep-copying
	 * it.
	 * 
	 * @param origin the origin to be copied
	 */
	public OptionSet(OptionSet origin) {
		if (origin == null) throw new NullPointerException();
		if_match_list       = copyList(origin.if_match_list);
		uri_host            = origin.uri_host;
		etag_list           = copyList(origin.etag_list);
		if_none_match       = origin.if_none_match;
		uri_port            = origin.uri_port;
		location_path_list  = copyList(origin.location_path_list);
		uri_path_list       = copyList(origin.uri_path_list);
		content_format      = origin.content_format;
		max_age             = origin.max_age;
		uri_query_list      = copyList(origin.uri_query_list);
		accept              = origin.accept;
		location_query_list = copyList(origin.location_query_list);
		proxy_uri           = origin.proxy_uri;
		proxy_scheme        = origin.proxy_scheme;
		
		if (origin.block1 != null)
			block1          = new BlockOption(origin.block1);
		if (origin.block2 != null)
			block2          = new BlockOption(origin.block2);
		
		observe = origin.observe;
		
		others              = copyList(origin.others);
	}
	
	/**
	 * Copy the specified list.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 * @return a copy of the list
	 */
	private <T> List<T> copyList(List<T> list) {
		if (list == null) return null;
		else return new LinkedList<T>(list);
	}
	
	/////////////////////// Getter and Setter ///////////////////////
	
	/**
	 * Ensures that there is an if_match_list.
	 * @return the list of opaque If-match options
	 */
	public List<byte[]> getIfMatchs() {
		if (if_match_list == null)
			synchronized (this) {
				if (if_match_list == null)
					if_match_list = new LinkedList<byte[]>();
			}
		return if_match_list;
	}
	
	public int getIfMatchCount() {
		return getIfMatchs().size();
	}
	
	public boolean getIfMatch(byte[] what) {
		
		// no If-Match option allows updates
		if (if_match_list==null) return true;
		
		for (byte[] etag:if_match_list) {
			if (Arrays.equals(etag, what)) return true;
		}
		return false;
	}
	
	public OptionSet addIfMatch(byte[] opaque) {
		if (opaque==null)
			throw new IllegalArgumentException("If-Match option must not be null");
		if (opaque.length > 8)
			throw new IllegalArgumentException("Content of If-Match option is too large: "+Utils.toHexString(opaque));
		getIfMatchs().add(opaque);
		return this;
	}
	
	public OptionSet removeIfMatch(byte[] opaque) {
		getIfMatchs().remove(opaque);
		return this;
	}
	
	public OptionSet clearIfMatchs() {
		getIfMatchs().clear();
		return this;
	}
	
	public String getURIHost() {
		return uri_host;
	}
	
	public boolean hasURIHost() {
		return uri_host != null;
	}
	
	public OptionSet setURIHost(String host) {
		if (host==null)
			throw new NullPointerException("URI-Host must not be null");
		if (host.length() < 1 || 255 < host.length())
			throw new IllegalArgumentException("URI-Host option's length must be between 1 and 255 inclusive");
		this.uri_host = host;
		return this;
	}
	
	public List<byte[]> getETags() {
		if (etag_list == null)
			synchronized (this) {
				if (etag_list == null)
					etag_list = new LinkedList<byte[]>();
			}
		return etag_list;
	}
	
	public int getETagCount() {
		return getETags().size();
	}
	
	public boolean containsETag(byte[] what) {
		if (etag_list==null) return false;
		for (byte[] etag:etag_list) {
			if (Arrays.equals(etag, what)) return true;
		}
		return false;
	}
	
	public OptionSet addETag(byte[] opaque) {
		if (opaque==null)
			throw new IllegalArgumentException("ETag option must not be null");
		// TODO: ProxyHttp uses ETags that are larger than 8 bytes (20).
//		if (opaque.length < 1 || 8 < opaque.length)
//			throw new IllegalArgumentException("ETag option's length must be between 1 and 8 inclusive but was "+opaque.length);
		getETags().add(opaque);
		return this;
	}
	
	public OptionSet removeETag(byte[] opaque) {
		getETags().remove(opaque);
		return this;
	}
	
	public OptionSet cleatETags() {
		getETags().clear();
		return this;
	}
	
	public boolean hasIfNoneMatch() {
		return if_none_match;
	}
	
	public OptionSet setIfNoneMatch(boolean b) {
		if_none_match = b;
		return this;
	}
	
	public Integer getURIPort() {
		return uri_port;
	}
	
	public boolean hasURIPort() {
		return uri_port != null;
	}
	
	public OptionSet setURIPort(int port) {
		if (port < 0 || (1<<16)-1 < port)
			throw new IllegalArgumentException("URI port option must be between 0 and "+((1<<16)-1)+" (2 bytes) inclusive but was "+port);
		uri_port = port;
		return this;
	}
	
	public OptionSet removeURIPort() {
		uri_port = null;
		return this;
	}
	
	public List<String> getLocationPaths() {
		if (location_path_list == null)
			synchronized (this) {
				if (location_path_list == null)
					location_path_list = new LinkedList<String>();
			}
		return location_path_list;
	}
	
	public String getLocationPathString() {
		StringBuilder builder = new StringBuilder();
		for (String segment:getLocationPaths())
			builder.append(segment).append("/");
		if (builder.length() > 0)
			builder.delete(builder.length() - 1, builder.length());
		return builder.toString();
	}
	
	public int getLocationPathCount() {
		return getLocationPaths().size();
	}
	
	public OptionSet addLocationPath(String path) {
		if (path == null)
			throw new IllegalArgumentException("Location path option must not be null");
		if (path.length() > 255)
			throw new IllegalArgumentException("Location path option's length must be between 0 and 255 inclusive");
		getLocationPaths().add(path);
		return this;
	}
	
	public OptionSet removeLocationPath(String path) {
		getLocationPaths().remove(path);
		return this;
	}
	
	public OptionSet clearLocationPaths() {
		getLocationPaths().clear();
		return this;
	}
	
	public OptionSet setLocationPath(String path) {
		String[] parts = path.split("/");
		for (String segment:parts)
			if (!segment.isEmpty())
				addLocationPath(segment);
		return this;
	}
	
	public List<String> getURIPaths() {
		if (uri_path_list == null)
			synchronized (this) {
				if (uri_path_list == null)
					uri_path_list = new LinkedList<String>();
			}
		return uri_path_list;
	}
	
	public String getURIPathString() {
		StringBuilder buffer = new StringBuilder();
		for (String element:getURIPaths())
			buffer.append(element).append("/");
		if (buffer.length()==0) return "";
		else return buffer.substring(0, buffer.length()-1);
	}
	
	public int getURIPathCount() {
		return getURIPaths().size();
	}
	
	public OptionSet setURIPath(String path) {
		if (path == null)
			throw new NullPointerException();
		String slash = "/";
		while (path.startsWith(slash)) {
			path = path.substring(slash.length());
		}
		
		clearURIPaths();
		for (String segment : path.split(slash)) {
			// empty path segments are allowed (e.g., /test vs /test/)
			if (!segment.isEmpty()) {
				addURIPath(segment);
			}
		}
		return this;
	}
	
	public OptionSet addURIPath(String path) {
		if (path == null)
			throw new IllegalArgumentException("URI path option must not be null");
		if (path.length() > 255)
			throw new IllegalArgumentException("URI path option's length must be between 0 and 255 inclusive");
		getURIPaths().add(path);
		return this;
	}
	
	public OptionSet removeURIPath(String path) {
		getURIPaths().remove(path);
		return this;
	}
	
	public OptionSet clearURIPaths() {
		getURIPaths().clear();
		return this;
	}
	
	public int getContentFormat() {
		return hasContentFormat() ? content_format : MediaTypeRegistry.UNDEFINED;
	}
	
	public boolean hasContentFormat() {
		return content_format != null;
	}

	public boolean hasContentFormat(int format) {
		return content_format != null && content_format == format;
	}
	
	/**
	 * 
	 * @param format
	 * @return
	 * @see MediaTypeRegistry
	 */
	public OptionSet setContentFormat(int format) {
		content_format = format;
		return this;
	}
	
	public OptionSet removeContentFormat() {
		content_format = null;
		return this;
	}
	
	public Long getMaxAge() {
		Long m = max_age;
		return m != null ? m : OptionNumberRegistry.DEFAULT_MAX_AGE;
	}
	
	// Remember that the absence of a Max-Age option means its
	// default value DEFAULT_MAX_AGE (60L).
	public boolean hasMaxAge() {
		return max_age != null;
	}
	
	public OptionSet setMaxAge(long age) {
		if (age < 0 || ((1L<<32)-1) < age)
			throw new IllegalArgumentException("Max-Age option must be between 0 and "+((1L<<32)-1)+" (4 bytes) inclusive");
		max_age = age;
		return this;
	}
	
	public OptionSet removeMaxAge() {
		max_age = null;
		return this;
	}
	
	public List<String> getURIQueries() {
		if (uri_query_list == null)
			synchronized (this) {
				if (uri_query_list == null)
					uri_query_list = new LinkedList<String>();
			}
		return uri_query_list;
	}
	
	public int getURIQueryCount() {
		return getURIQueries().size();
	}
	
	public OptionSet setURIQuery(String query) {
		if (query == null)
			throw new NullPointerException();
		String ampersand = "&";
		while (query.startsWith(ampersand)) {
			query = query.substring(ampersand.length());
		}
		
		clearURIQuery();
		for (String segment : query.split(ampersand)) {
			// empty path segments are allowed (e.g., /test vs /test/)
			if (!segment.isEmpty()) {
				addURIQuery(segment);
			}
		}
		return this;
	}
	
	public String getURIQueryString() {
		StringBuilder builder = new StringBuilder();
		for (String query:getURIQueries())
			builder.append(query).append("&");
		if (builder.length() > 0)
			builder.delete(builder.length() - 1, builder.length());
		return builder.toString();
	}
	
	public OptionSet addURIQuery(String query) {
		if (query == null)
			throw new NullPointerException("URI-Query option must not be null");
		if (query.length() > 255)
			throw new IllegalArgumentException("URI-Qurty option's length must be between 0 and 255 inclusive");
		getURIQueries().add(query);
		return this;
	}
	
	public OptionSet removeURIQuery(String query) {
		getURIQueries().remove(query);
		return this;
	}
	
	public OptionSet clearURIQuery() {
		getURIQueries().clear();
		return this;
	}
	
	public int getAccept() {
		return hasAccept() ? accept : MediaTypeRegistry.UNDEFINED;
	}
	
	public boolean hasAccept() {
		return accept != null;
	}
	
	/**
	 * 
	 * @param acc
	 * @return
	 * @see MediaTypeRegistry
	 */
	public OptionSet setAccept(int acc) {
		if (acc < 0 || acc > ((1<<16)-1))
			throw new IllegalArgumentException("Accept option must be between 0 and "+((1<<16)-1)+" (2 bytes) inclusive");
		accept = acc;
		return this;
	}
	
	public OptionSet removeAccept() {
		accept = null;
		return this;
	}
	
	public List<String> getLocationQueries() {
		if (location_query_list == null)
			synchronized (this) {
				if (location_query_list == null)
					location_query_list = new LinkedList<String>();
			}
		return location_query_list;
	}
	
	public String getLocationQueryString() {
		StringBuilder builder = new StringBuilder();
		for (String query:getLocationQueries())
			builder.append(query).append("&");
		if (builder.length() > 0)
			builder.delete(builder.length() - 1, builder.length());
		return builder.toString();
	}
	
	public OptionSet setLocationQuery(String query) {
		if (query.startsWith("?"))
			query = query.substring(1);
		String[] parts = query.split("&");
		for (String segment:parts)
			if (!segment.isEmpty())
				addLocationQuery(segment);
		return this;
	}
	
	public int getLocationQueryCount() {
		return getLocationQueries().size();
	}
	
	public OptionSet addLocationQuery(String query) {
		if (query == null)
			throw new NullPointerException("Location Query option must not be null");
		if (query.length() > 255)
			throw new IllegalArgumentException("Location Query option's length must be between 0 and 255 inclusive");
		getLocationQueries().add(query);
		return this;
	}
	
	public OptionSet removeLocationQuery(String query) {
		getLocationQueries().remove(query);
		return this;
	}
	
	public OptionSet clearLocationQuery() {
		getLocationQueries().clear();
		return this;
	}
	
	public String getProxyURI() {
		return proxy_uri;
	}
	
	public boolean hasProxyURI() {
		return proxy_uri != null;
	}
	
	public OptionSet setProxyURI(String uri) {
		if (uri == null)
			throw new NullPointerException("Proxy URI option must not be null");
		if (uri.length() < 1 || 1034 < uri.length())
			throw new IllegalArgumentException();
		proxy_uri = uri;
		return this;
	}
	
	public OptionSet removeProxyURI() {
		proxy_uri = null;
		return this;
	}
	
	public String getProxyScheme() {
		return proxy_scheme;
	}
	
	public boolean hasProxyScheme() {
		return proxy_scheme != null;
	}
	
	public OptionSet setProxyScheme(String scheme) {
		if (scheme == null)
			throw new NullPointerException("Proxy Scheme option must not be null");
		if (scheme.length() < 1 || 255 < scheme.length())
			throw new IllegalArgumentException("Proxy Scheme option's length must be between 1 and 255 inclusive");
		proxy_scheme = scheme;
		return this;
	}
	
	public OptionSet clearProxyScheme() {
		proxy_scheme = null;
		return this;
	}
	
	public BlockOption getBlock1() {
		return block1;
	}
	
	public boolean hasBlock1() {
		return block1 != null;
	}

	public void setBlock1(int szx, boolean m, int num) {
		this.block1 = new BlockOption(szx, m, num);
	}
	
	public void setBlock1(byte[] value) {
		this.block1 = new BlockOption(value);
	}
	
	public void setBlock1(BlockOption block1) {
		this.block1 = block1;
	}
	
	public void removeBlock1() {
		this.block1 = null;
	}

	public BlockOption getBlock2() {
		return block2;
	}
	
	public boolean hasBlock2() {
		return block2 != null;
	}

	public void setBlock2(int szx, boolean m, int num) {
		this.block2 = new BlockOption(szx, m, num);
	}
	
	public void setBlock2(byte[] value) {
		this.block2 = new BlockOption(value);
	}
	
	public void setBlock2(BlockOption block2) {
		this.block2 = block2;
	}
	
	public void removeBlock2() {
		this.block2 = null;
	}
	
	public Integer getObserve() {
		return observe;
	}
	
	public boolean hasObserve() {
		return observe != null;
	}
	
	public OptionSet setObserve(int observe) {
		if (observe <0 || ((1 << 24) - 1) < observe)
			throw new IllegalArgumentException("Observe option must be between 0 and "+((1<<24)-1)+" (3 bytes) inclusive but was "+observe);
		this.observe = observe;
		return this;
	}
	
	public OptionSet removeObserve() {
		observe = null;
		return this;
	}
	
	public boolean hasOption(int number) {
		return Collections.binarySearch(asSortedList(), new Option(number)) >= 0;
	}
	
	private List<Option> getOthers() {
		if (others == null)
			synchronized (this) {
				if (others == null)
					others = new LinkedList<Option>();
			}
		return others;
	}
	
	/**
	 * Returns all options in a list sorted according to their option numbers.
	 * 
	 * @return the sorted list
	 */
	public List<Option> asSortedList() {
		ArrayList<Option> options = new ArrayList<Option>();
		if (if_match_list != null) for (byte[] value:if_match_list)
			options.add(new Option(CoAP.OptionRegistry.IF_MATCH, value));
		if (hasURIHost())
			options.add(new Option(CoAP.OptionRegistry.URI_HOST, getURIHost()));
		if (etag_list != null) for (byte[] value:etag_list)
			options.add(new Option(CoAP.OptionRegistry.ETAG, value));
		if (hasIfNoneMatch())
			options.add(new Option(CoAP.OptionRegistry.IF_NONE_MATCH));
		if (hasURIPort())
			options.add(new Option(CoAP.OptionRegistry.URI_PORT, getURIPort()));
		if (location_path_list != null) for (String str:location_path_list)
			options.add(new Option(CoAP.OptionRegistry.LOCATION_PATH, str));
		if (uri_path_list != null) for (String str:uri_path_list)
			options.add(new Option(CoAP.OptionRegistry.URI_PATH, str));
		if (hasContentFormat())
			options.add(new Option(CoAP.OptionRegistry.CONTENT_FORMAT, getContentFormat()));
		if (hasMaxAge())
			options.add(new Option(CoAP.OptionRegistry.MAX_AGE, getMaxAge()));
		if (uri_query_list != null) for (String str:uri_query_list)
			options.add(new Option(CoAP.OptionRegistry.URI_QUERY, str));
		if (hasAccept())
			options.add(new Option(CoAP.OptionRegistry.ACCEPT, getAccept()));
		if (location_query_list != null) for (String str:location_query_list)
			options.add(new Option(CoAP.OptionRegistry.LOCATION_QUERY, str));
		if (hasProxyURI())
			options.add(new Option(CoAP.OptionRegistry.PROXY_URI, getProxyURI()));
		if (hasProxyScheme())
			options.add(new Option(CoAP.OptionRegistry.PROXY_SCHEME, getProxyScheme()));
		
		if (hasBlock1())
			options.add(new Option(CoAP.OptionRegistry.BLOCK1, getBlock1().getValue()));
		if (hasBlock2())
			options.add(new Option(CoAP.OptionRegistry.BLOCK2, getBlock2().getValue()));
		
		if (hasObserve())
			options.add(new Option(CoAP.OptionRegistry.OBSERVE, getObserve()));
		
		if (others != null)
			options.addAll(others);

		Collections.sort(options);
		return options;
	}

	// Arbitrary or CoAP defined option
	public OptionSet addOption(Option o) {
		getOthers().add(o);
		return this;
	}
	
	@Override
	public String toString() {
		List<String> os = new ArrayList<String>();
		if (if_match_list != null && getIfMatchCount() > 0)
			os.add("If-Match="+toHexString(if_match_list));
		if (hasURIHost())
			os.add("URI-Host="+uri_host);
		if (etag_list != null && getETagCount() > 0)
			os.add("ETag="+toHexString(etag_list));
		if (hasIfNoneMatch())
			os.add("If-None-Match="+if_none_match);
		if (hasURIPort())
			os.add("URI-Port="+uri_port);
		if (location_path_list != null && getLocationPathCount() > 0)
			os.add("Location-Path="+Arrays.toString(location_path_list.toArray()));
		if (uri_path_list != null && getURIPathCount() > 0)
			os.add("URI-Path="+Arrays.toString(uri_path_list.toArray()));
		if (hasContentFormat())
			os.add("Content-Format="+content_format);
		if (hasMaxAge() /*&& max_age != CoAP.OptionRegistry.Default.MAX_AGE*/)
			os.add("Max-Age="+max_age);
		if (uri_query_list != null && getURIQueryCount() > 0)
			os.add("URI-Query="+Arrays.toString(uri_query_list.toArray()));
		if (hasAccept())
			os.add("Accept="+accept);
		if (location_query_list != null && getLocationQueryCount() > 0)
			os.add("Location-Query="+Arrays.toString(location_query_list.toArray()));
		if (hasProxyURI())
			os.add("Proxy-URI="+proxy_uri);
		if (hasProxyScheme())
			os.add("Proxy-Scheme="+proxy_scheme);

		if (hasBlock1())
			os.add("Block1="+block1);
		if (hasBlock2())
			os.add("Block2="+block2);
		
		if (hasObserve())
			os.add("Observe="+observe);
		
		if (others != null)
			for (Option o:others)
				os.add(o.toString());
		
		return "OptionSet="+Arrays.toString(os.toArray());
	}
		
	/**
	 * Converts a list of byte arrays to a string where each byte array is
	 * represented in hexadecimal code.
	 * 
	 * @param list the list of byte arrays
	 * @return the string with hexadecimal encoding.
	 */
	private String toHexString(List<byte[]> list) {
		List<String> hexs = new ArrayList<String>(list.size());
		for (byte[] bytes:list)
			hexs.add(toHexString(bytes));
		return Arrays.toString(hexs.toArray());
	}
	
	/**
	 * Converts the specified byte array to a hexadecimal string.
	 *
	 * @param bytes the byte array
	 * @return the hexadecimal code string
	 */
	private String toHexString(byte[] bytes) {
		   StringBuilder sb = new StringBuilder();
		   for(byte b:bytes)
		      sb.append(String.format("%02x", b & 0xFF));
		   return sb.toString();
	}
}
