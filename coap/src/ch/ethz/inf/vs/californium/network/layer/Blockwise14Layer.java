package ch.ethz.inf.vs.californium.network.layer;

import java.util.logging.Logger;

import ch.ethz.inf.vs.californium.coap.BlockOption;
import ch.ethz.inf.vs.californium.coap.CoAP.Code;
import ch.ethz.inf.vs.californium.coap.CoAP.ResponseCode;
import ch.ethz.inf.vs.californium.coap.CoAP.Type;
import ch.ethz.inf.vs.californium.coap.EmptyMessage;
import ch.ethz.inf.vs.californium.coap.Message;
import ch.ethz.inf.vs.californium.coap.MessageObserverAdapter;
import ch.ethz.inf.vs.californium.coap.OptionSet;
import ch.ethz.inf.vs.californium.coap.Request;
import ch.ethz.inf.vs.californium.coap.Response;
import ch.ethz.inf.vs.californium.network.Exchange;
import ch.ethz.inf.vs.californium.network.config.NetworkConfig;
import ch.ethz.inf.vs.californium.network.config.NetworkConfigDefaults;
import ch.ethz.inf.vs.californium.network.config.NetworkConfigObserverAdapter;

public class Blockwise14Layer extends AbstractLayer {

	/** The logger. */
	protected final static Logger LOGGER = Logger.getLogger(Blockwise14Layer.class.getCanonicalName());
	
	// TODO: Size Option. Include only in first block.
	// TODO: DoS: server should have max allowed blocks/bytes/time to allocate.
	// TODO: Random access for Cf servers: The draft still needs to specify a reaction to "overshoot"
	// TODO: Blockwise with separate response or NONs. Not yet mentioned in draft.
	//       Separate responses will have trouble with blockwise PUT/POSTs.
	// TODO: How should our client deal with a server that handles blocks non-atomic?
	// TODO: Forward cancellation and timeouts of a request to its blocks.
	
	/**
	 * What if a request contains a Block2 option with size 128 but the response
	 * is only 10 bytes long? Should we still add the block2 option to the
	 * response? Currently, we do.
	 * <p>
	 * The draft needs to specify whether it is allowed to use separate
	 * responses or NONs. Otherwise, I do not know whether I should allow (or
	 * prevent) the resource to use it. Currently, we do not prevent it but I am
	 * not sure what would happen if a resource used accept() or NONs.
	 * <p>
	 * What is the client supposed to do when it asks the server for block x but
	 * receives a wrong block? The client cannot send a 4.08 (Request Entity
	 * Incomplete). Should it reject it? Currently, we reject it and cancel the
	 * request.
	 * <p>
	 * In a blockwise transfer of a response to a POST request, the draft should
	 * mention whether the client should always include all options in each
	 * request for the next block or not. The response is already produced at
	 * the server, thus, there is no point in receiving them again. The draft
	 * only states that the payload should be empty. Currently we always send
	 * all options in each request (just in case) (except observe which is not
	 * allowed).
	 * <p>
	 * When an observe notification is being sent blockwise, it is not clear
	 * whether we are allowed to include the observe option in each response
	 * block. In the draft, the observe option is left out but it would be
	 * easier for us if we were allowed to include it. The policy which options
	 * should be included in which block is not clear to me anyway. ETag is
	 * always included, observe only in the first block, what about the others?
	 * Currently, I send observe only in the first block so that it exactly
	 * matches the example in the draft.
	 */
	
	private int maxMsgSize;
	private int defaultBlockSize;
	
	public Blockwise14Layer(NetworkConfig config) {
		this.maxMsgSize = config.getInt(NetworkConfigDefaults.MAX_MESSAGE_SIZE);
		this.defaultBlockSize = config.getInt(NetworkConfigDefaults.DEFAULT_BLOCK_SIZE);
		LOGGER.config("Blockwise14 layer uses MAX_MESSAGE_SIZE: "+maxMsgSize+" and DEFAULT_BLOCK_SIZE:"+defaultBlockSize);
		
		config.addConfigObserver(new NetworkConfigObserverAdapter() {
			@Override
			public void changed(String key, int value) {
				if (NetworkConfigDefaults.MAX_MESSAGE_SIZE.equals(key))
					maxMsgSize = value;
				if (NetworkConfigDefaults.DEFAULT_BLOCK_SIZE.equals(key))
					defaultBlockSize = value;
			}
		});
	}
	
	@Override
	public void sendRequest(Exchange exchange, Request request) {
		if (requiresBlockwise(request)) {
			// This must be a large POST or PUT request
			LOGGER.info("Request payload "+request.getPayloadSize()+"/"+maxMsgSize+" requires Blockwise");
			BlockwiseStatus status = findRequestBlockStatus(exchange);
			
			Request block = getNextRequestBlock(request, status);
			
			exchange.setRequestBlockStatus(status);
			exchange.setCurrentRequest(block);
			super.sendRequest(exchange, block);
			
		} else {
			exchange.setCurrentRequest(request);
			super.sendRequest(exchange, request);
		}
	}

	@Override
	public void receiveRequest(Exchange exchange, Request request) {
		if (request.getOptions().hasBlock1()) {
			// This must be a large POST or PUT request
			BlockOption block1 = request.getOptions().getBlock1();
			LOGGER.fine("Request contains block1 option "+block1);
			
			BlockwiseStatus status = findRequestBlockStatus(exchange);
			if (block1.getNum() == 0 && status.getCurrentNum() > 0) {
				LOGGER.fine("Block1 num is 0, the client has restarted the blockwise transfer. Reset status.");
				status = new BlockwiseStatus();
				exchange.setRequestBlockStatus(status);
			}
			
			if (block1.getNum() == status.getCurrentNum()) {
				status.addBlock(request.getPayload());
				status.setCurrentNum(status.getCurrentNum() + 1);
				if ( block1.isM() ) {
					LOGGER.fine("There are more blocks to come. Acknowledge this block.");
					
					if (request.isConfirmable()) {
						Response piggybacked = Response.createPiggybackedResponse(request, ResponseCode.CONTINUE);
						piggybacked.getOptions().setBlock1(block1.getSzx(), true, block1.getNum());
						piggybacked.setLast(false);
						request.setAcknowledged(true);
						exchange.setCurrentResponse(piggybacked);
						super.sendResponse(exchange, piggybacked);
					}
					// do not assemble and deliver the request yet
					
				} else {
					LOGGER.fine("This was the last block. Deliver request");
					
					// Remember block to acknowledge. TODO: We might make this a boolean flag in status.
					exchange.setBlock1ToAck(block1); 
					
					// Block2 early negotiation
					earlyBlock2Negotiation(exchange, request);
					
					// Assemble and deliver
					Request assembled = new Request(request.getCode()); // getAssembledRequest(status, request);
					assembleMessage(status, assembled, request);
//					assembled.setAcknowledged(true); // TODO: prevents accept from sending ACK. smart?
					exchange.setRequest(assembled);
					super.receiveRequest(exchange, assembled);
				}
				
			} else {
				// ERROR, wrong number, Incomplete
				LOGGER.warning("Wrong block number. Expected "+status.getCurrentNum()+" but received "+block1.getNum()+". Respond with 4.08 (Request Entity Incomplete)");
				Response error = Response.createPiggybackedResponse(request, ResponseCode.REQUEST_ENTITY_INCOMPLETE);
				error.getOptions().setBlock1(block1.getSzx(), block1.isM(), block1.getNum());
				request.setAcknowledged(true);
				exchange.setCurrentResponse(error);
				super.sendResponse(exchange, error);
			}
			
		} else if (exchange.getResponse()!=null && request.getOptions().hasBlock2()) {
			// The response has already been generated and the client just wants
			// the next block of it
			BlockOption block2 = request.getOptions().getBlock2();
			Response response = exchange.getResponse();
			BlockwiseStatus status = findResponseBlockStatus(exchange);
			status.setCurrentNum(block2.getNum());
			status.setCurrentSzx(block2.getSzx());
			
			Response block = getNextResponsesBlock(response, status);
			block.setToken(request.getToken());

			// TODO: Are we allowed to NOT remove the observe option?
			if (status.getCurrentNum() > 0)
				block.getOptions().removeObserve();
			
			// This is necessary for notifications that are sent blockwise:
			if (status.isComplete())
				status.setCurrentNum(0);
			
			exchange.setCurrentResponse(block);
			super.sendResponse(exchange, block);
			
		} else {
			earlyBlock2Negotiation(exchange, request);

			exchange.setRequest(request);
			super.receiveRequest(exchange, request);
		}
	}

	@Override
	public void sendResponse(Exchange exchange, Response response) {
		BlockOption block1 = exchange.getBlock1ToAck();
		if (block1 != null)
			exchange.setBlock1ToAck(null);
		
		if (requireBlockwise(exchange, response)) {
			// This must be a large response to a GET or POST request (PUT?)
			LOGGER.info("Response payload "+response.getPayloadSize()+"/"+maxMsgSize+" requires Blockwise");
			
			BlockwiseStatus status = findResponseBlockStatus(exchange);
			
			Response block = getNextResponsesBlock(response, status);
			if (block1 != null) // in case we still have to ack the last block1
				block.getOptions().setBlock1(block1);
			if (block.getToken() == null)
				block.setToken(exchange.getRequest().getToken());
			
			exchange.setCurrentResponse(block);
			super.sendResponse(exchange, block);
			
		} else {
			if (block1 != null) response.getOptions().setBlock1(block1);
			exchange.setCurrentResponse(response);
			super.sendResponse(exchange, response);
		}
	}
	
	@Override
	public void receiveResponse(Exchange exchange, Response response) {
		if (!response.getOptions().hasBlock1() && !response.getOptions().hasBlock2()) {
			// There is no block1 or block2 option, therefore it is a normal response
			exchange.setResponse(response);
			super.receiveResponse(exchange, response);
			return;
		}
		
		if (response.getOptions().hasBlock1()) {
			// TODO: What if request has not been sent blockwise (server error)
			BlockOption block1 = response.getOptions().getBlock1();
			LOGGER.fine("Response acknowledges block "+block1);
			
			BlockwiseStatus status = exchange.getRequestBlockStatus();
			if (! status.isComplete()) {
				// TODO: the response code should be CONTINUE. Otherwise deliver
				// Send next block
				int currentSize = 1 << (4 + status.getCurrentSzx());
				int nextNum = status.getCurrentNum() + currentSize / block1.getSize();
				LOGGER.fine("Send next block num = "+nextNum);
				status.setCurrentNum(nextNum);
				status.setCurrentSzx(block1.getSzx());
				Request nextBlock = getNextRequestBlock(exchange.getRequest(), status);
				if (nextBlock.getToken() == null)
					nextBlock.setToken(response.getToken()); // reuse same token
				exchange.setCurrentRequest(nextBlock);
				super.sendRequest(exchange, nextBlock);
				// do not deliver response
				
			} else if (!response.getOptions().hasBlock2()) {
				// All request block have been acknowledged and we receive a piggy-backed
				// response that needs no blockwise transfer. Thus, deliver it.
				super.receiveResponse(exchange, response);
			} else {
				LOGGER.fine("Response has Block2 option and is therefore sent blockwise");
			}
		}
		
		if (response.getOptions().hasBlock2()) {
			BlockOption block2 = response.getOptions().getBlock2();
			BlockwiseStatus status = findResponseBlockStatus(exchange);
			
			if (block2.getNum() == status.getCurrentNum()) {
				// We got the block we expected :-)
				status.addBlock(response.getPayload());
				if (response.getOptions().hasObserve())
					status.setObserve(response.getOptions().getObserve());
				
				if (block2.isM()) {
					LOGGER.fine("Request the next response block");
					// TODO: If this is a notification, do we have to use
					// another token now?

					Request request = exchange.getRequest();
					int num = block2.getNum() + 1;
					int szx = block2.getSzx();
					boolean m = false;
					Request block = new Request(request.getCode());
					block.setOptions(new OptionSet(request.getOptions()));
					block.setDestination(request.getDestination());
					block.setDestinationPort(request.getDestinationPort());
					block.setToken(request.getToken());
					block.setType(request.getType()); // NON could make sense over SMS or similar transports
					block.getOptions().setBlock2(szx, m, num);
					status.setCurrentNum(num);
					
					// TODO: Are we allowed to NOT remove the observe option?
					block.getOptions().removeObserve();
					
					exchange.setCurrentRequest(block);
					super.sendRequest(exchange, block);
					
				} else {
					LOGGER.fine("We have received all "+status.getBlockCount()+" blocks of the response. Assemble and deliver");
					Response assembled = new Response(response.getCode());
					assembleMessage(status, assembled, response);
					assembled.setType(response.getType());
					
					// Check if this response is a notification
					int observe = status.getObserve();
					if (observe != BlockwiseStatus.NO_OBSERVE) {
						assembled.getOptions().setObserve(observe);
						// This is necessary for notifications that are sent blockwise:
						// Reset block number AND container with all blocks
						exchange.setResponseBlockStatus(null);
					}
					
					LOGGER.fine("Assembled response: "+assembled);
					exchange.setResponse(assembled);
					super.receiveResponse(exchange, assembled);
				}
				
			} else {
				// ERROR, wrong block number (server error)
				// TODO: This scenario is not specified in the draft.
				// Currently, we reject it and cancel the request.
				LOGGER.warning("Wrong block number. Expected "+status.getCurrentNum()+" but received "+block2.getNum()+". Reject response; exchange has failed.");
				EmptyMessage rst = EmptyMessage.newRST(response);
				super.sendEmptyMessage(exchange, rst);
				exchange.getRequest().cancel();
			}
		}
	}

	@Override
	public void sendEmptyMessage(Exchange exchange, EmptyMessage message) {
		super.sendEmptyMessage(exchange, message);
	}

	@Override
	public void receiveEmptyMessage(Exchange exchange, EmptyMessage message) {
		super.receiveEmptyMessage(exchange, message);
	}
	
	/////////// HELPER METHODS //////////
	
	private void earlyBlock2Negotiation(Exchange exchange, Request request) {
		// Call this method when a request has completely arrived (might have
		// been sent in one piece without blockwise).
		if (request.getOptions().hasBlock2()) {
			BlockOption block2 = request.getOptions().getBlock2();
			LOGGER.fine("Request demands blockwise transfer of response with option "+block2+". Create and set new block2 status");
			BlockwiseStatus status2 = new BlockwiseStatus(block2.getNum(), block2.getSzx());
			exchange.setResponseBlockStatus(status2);
		}
	}
	
	private BlockwiseStatus findRequestBlockStatus(Exchange exchange) {
		// NOTICE: This method is used by sendRequest and receiveRequest. Be
		// careful, making changes to the status in here.
		BlockwiseStatus status = exchange.getRequestBlockStatus();
		if (status == null) {
			status = new BlockwiseStatus();
			status.setCurrentSzx( computeSZX(defaultBlockSize) );
			exchange.setRequestBlockStatus(status);
			LOGGER.fine("There is no assembler status yet. Create and set new block1 status: "+status);
		}
		return status;
	}
	
	private BlockwiseStatus findResponseBlockStatus(Exchange exchange) {
		// NOTICE: This method is used by sendResponse and receiveResponse. Be
		// careful, making changes to the status in here.
		BlockwiseStatus status = exchange.getResponseBlockStatus();
		if (status == null) {
			status = new BlockwiseStatus();
			status.setCurrentSzx( computeSZX(defaultBlockSize) );
			exchange.setResponseBlockStatus(status);
			LOGGER.fine("There is no blockwise status yet. Create and set new block2 status: "+status);
		} else {
			LOGGER.fine("Current blockwise status: "+status);
		}
		return status;
	}
	
	private Request getNextRequestBlock(Request request, BlockwiseStatus status) {
		int num = status.getCurrentNum();
		int szx = status.getCurrentSzx();
		Request block = new Request(request.getCode());
		block.setOptions(new OptionSet(request.getOptions()));
		block.setDestination(request.getDestination());
		block.setDestinationPort(request.getDestinationPort());
		block.setToken(request.getToken());
		block.setType(Type.CON);
		
		int currentSize = 1 << (4 + szx);
		int from = num * currentSize;
		int to = Math.min((num + 1) * currentSize, request.getPayloadSize());
		int length = to - from;
		byte[] blockPayload = new byte[length];
		System.arraycopy(request.getPayload(), from, blockPayload, 0, length);
		block.setPayload(blockPayload);
		
		boolean m = (to < request.getPayloadSize());
		block.getOptions().setBlock1(szx, m, num);
		
		status.setComplete(!m);
		return block;
	}
	
	private Response getNextResponsesBlock(Response response, BlockwiseStatus status) {
		int szx = status.getCurrentSzx();
		int num = status.getCurrentNum();
		Response block = new Response(response.getCode());
//		block.setType(response.getType());
		block.setDestination(response.getDestination());
		block.setDestinationPort(response.getDestinationPort());
		block.setToken(response.getToken());
		block.setOptions(new OptionSet(response.getOptions()));
		block.addMessageObserver(new TimeoutForwarder(response));
		
		if (response.getPayloadSize() > 0) {
			int currentSize = 1 << (4 + szx);
			int from = num * currentSize;
			int to = Math.min((num + 1) * currentSize, response.getPayloadSize());
			int length = to - from;
			byte[] blockPayload = new byte[length];
			System.arraycopy(response.getPayload(), from, blockPayload, 0, length);
			block.setPayload(blockPayload);
			
			boolean m = (to < response.getPayloadSize());
			block.getOptions().setBlock2(szx, m, num);
			block.setLast(!m);
			
			status.setComplete(!m);
		} else {
			block.getOptions().setBlock2(szx, false, 0);
			block.setLast(true);
			status.setComplete(true);
		}
		return block;
	}
	
	private void assembleMessage(BlockwiseStatus status, Message message, Message last) {
		// The assembled request will contain the options of the last block
		message.setMID(last.getMID());
		message.setSource(last.getSource());
		message.setSourcePort(last.getSourcePort());
		message.setToken(last.getToken());
		message.setType(last.getType());
		message.setOptions(new OptionSet(last.getOptions()));
		
		int length = 0;
		for (byte[] block:status.getBlocks())
			length += block.length;
		
		byte[] payload = new byte[length];
		int offset = 0;
		for (byte[] block:status.getBlocks()) {
			System.arraycopy(block, 0, payload, offset, block.length);
			offset += block.length;
		}
		
		message.setPayload(payload);
	}
	
	private boolean requiresBlockwise(Request request) {
		if (request.getCode() == Code.PUT || request.getCode() == Code.POST) {
			return request.getPayloadSize() > maxMsgSize;
		} else return false;
	}
	
	private boolean requireBlockwise(Exchange exchange, Response response) {
		return response.getPayloadSize() > maxMsgSize
				|| exchange.getResponseBlockStatus() != null;
	}
	
	/**
	 * Encodes a block size into a 3-bit SZX value as specified by
	 * draft-ietf-core-block-03, section-2.1:
	 * 
	 * 16 bytes = 2^4 --> 0
	 * ... 
	 * 1024 bytes = 2^10 -> 6
	 * 
	 */
	private int computeSZX(int blockSize) {
		return (int)(Math.log(blockSize)/Math.log(2)) - 4;
	}
	
	// When a timeout occurs for a block it has to be forwarded to the origin
	// response.
	public static class TimeoutForwarder extends MessageObserverAdapter {
		
		private Message message;
		
		public TimeoutForwarder(Message message) {
			this.message = message;
		}
		
		@Override
		public void timeouted() {
			message.setTimeouted(true);
		}
	}
}
