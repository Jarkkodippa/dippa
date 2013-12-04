package ch.ethz.inf.vs.californium.observe;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * This class represents an observing endpoint. It holds all observe relations
 * that the endpoint has to this server. If a confirmable notification timeouts
 * for the maximum times allowed the server assumes the client is no longer
 * reachable and cancels all relations that it has established to resources.
 */
public class ObservingEndpoint {
	
	/** The endpoint's address */
	private final InetSocketAddress address;

	/** The list of relations the endpoint has established with this server */
	private final List<ObserveRelation> relations;
	
	/**
	 * Constructs a new ObservingEndpoint.
	 * @param address the endpoint's address
	 */
	public ObservingEndpoint(InetSocketAddress address) {
		this.address = address;
		this.relations = new CopyOnWriteArrayList<ObserveRelation>();
	}
	
	/**
	 * Adds the specified observe relation.
	 * @param relation the relation
	 */
	public void addObserveRelation(ObserveRelation relation) {
		relations.add(relation);
	}
	
	/**
	 * Removes the specified observe relations.
	 * @param relation the relation
	 */
	public void removeObserveRelation(ObserveRelation relation) {
		relations.remove(relation);
	}
	
	/**
	 * Cancels all observe relations that this endpoint has established with
	 * resources from this server.
	 */
	public void cancelAll() {
		for (ObserveRelation relation:relations)
			relation.cancel();
	}

	/**
	 * Returns the address of this endpoint-
	 * @return the address
	 */
	public InetSocketAddress getAddress() {
		return address;
	}
	
	/*
	 * This class is obsolete now since observe-09 where a client can have
	 * multiple observe relations with the same resource. Furthermore, the
	 * methods above have become much simpler since there is close to no
	 * bookkeeping required.
	 */
//	private static class ResourcePath {
//		
//		private final List<String> path;
//		
//		private ResourcePath(List<String> path) {
//			if (path == null)
//				throw new NullPointerException();
//			this.path = path;
//		}
//		
//		@Override
//		public boolean equals(Object o) {
//			if (! (o instanceof ResourcePath))
//				return false;
//			ResourcePath rp = (ResourcePath) o;
//			return path.equals(rp.path);
//		}
//		
//		@Override
//		public int hashCode() {
//			return path.hashCode();
//		}
//	}
}
