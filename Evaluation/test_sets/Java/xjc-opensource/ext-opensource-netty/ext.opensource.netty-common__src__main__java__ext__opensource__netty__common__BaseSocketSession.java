package ext.opensource.netty.common;

import java.io.Serializable;
import java.net.SocketAddress;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelConfig;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelMetadata;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.util.AttributeKey;

/**
 * @author ben
 * @Title: basic
 * @Description:
 **/

public abstract class BaseSocketSession implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private final transient Channel channel;

	public BaseSocketSession(Channel channel) {
		this.channel = channel;
	}

	public <T> void setAttribute(String name, T value) {
		AttributeKey<T> sessionIdKey = AttributeKey.valueOf(name);
		channel.attr(sessionIdKey).set(value);
	}

	public <T> T getAttribute(String name) {
		AttributeKey<T> sessionIdKey = AttributeKey.valueOf(name);
		return channel.attr(sessionIdKey).get();
	}

	public Channel channel() {
		return channel;
	}

	/**
	 * Returns the globally unique identifier of this {@link Channel}.
	 */
	public ChannelId id() {
		return channel.id();
	}

	/**
	 * Returns the configuration of this channel.
	 */
	public ChannelConfig config() {
		return channel.config();
	}

	/**
	 * Returns {@code true} if the {@link Channel} is open and may get active later
	 */
	public boolean isOpen() {
		return channel.isOpen();
	}

	/**
	 * Returns {@code true} if the {@link Channel} is registered with an
	 * {@link EventLoop}.
	 */
	public boolean isRegistered() {
		return channel.isRegistered();
	}

	/**
	 * Return {@code true} if the {@link Channel} is active and so connected.
	 */
	public boolean isActive() {
		return channel.isActive();
	}

	/**
	 * Return the {@link ChannelMetadata} of the {@link Channel} which describe the
	 * nature of the {@link Channel}.
	 */
	public ChannelMetadata metadata() {
		return channel.metadata();
	}

	/**
	 * Returns the local address where this channel is bound to. The returned
	 * {@link SocketAddress} is supposed to be down-cast into more concrete type
	 * such as {@link InetSocketAddress} to retrieve the detailed information.
	 *
	 * @return the local address of this channel. {@code null} if this channel is
	 *         not bound.
	 */
	public SocketAddress localAddress() {
		return channel.localAddress();
	}

	/**
	 * Returns the remote address where this channel is connected to. The returned
	 * {@link SocketAddress} is supposed to be down-cast into more concrete type
	 * such as {@link InetSocketAddress} to retrieve the detailed information.
	 *
	 * @return the remote address of this channel. {@code null} if this channel is
	 *         not connected. If this channel is not connected but it can receive
	 *         messages from arbitrary remote addresses (e.g.
	 *         {@link DatagramChannel}, use {@link DatagramPacket#recipient()} to
	 *         determine the origination of the received message as this method will
	 *         return {@code null}.
	 */
	public SocketAddress remoteAddress() {
		return channel.remoteAddress();
	}

	/**
	 * Returns the {@link ChannelFuture} which will be notified when this channel is
	 * closed. This method always returns the same future instance.
	 */
	public ChannelFuture closeFuture() {
		return channel.closeFuture();
	}

	/**
	 * Returns {@code true} if and only if the I/O thread will perform the requested
	 * write operation immediately. Any write requests made when this method returns
	 * {@code false} are queued until the I/O thread is ready to process the queued
	 * write requests.
	 */
	public boolean isWritable() {
		return channel.isWritable();
	}

	/**
	 * Get how many bytes can be written until {@link #isWritable()} returns
	 * {@code false}. This quantity will always be non-negative. If
	 * {@link #isWritable()} is {@code false} then 0.
	 */
	public long bytesBeforeUnwritable() {
		return channel.bytesBeforeUnwritable();
	}

	/**
	 * Get how many bytes must be drained from underlying buffers until
	 * {@link #isWritable()} returns {@code true}. This quantity will always be
	 * non-negative. If {@link #isWritable()} is {@code true} then 0.
	 */
	public long bytesBeforeWritable() {
		return channel.bytesBeforeWritable();
	}

	/**
	 * Returns an <em>internal-use-only</em> object that provides unsafe operations.
	 */
	public Channel.Unsafe unsafe() {
		return channel.unsafe();
	}

	/**
	 * Return the assigned {@link ChannelPipeline}.
	 */
	public ChannelPipeline pipeline() {
		return channel.pipeline();
	}

	/**
	 * Return the assigned {@link ByteBufAllocator} which will be used to allocate
	 * {@link ByteBuf}s.
	 */
	public ByteBufAllocator alloc() {
		return channel.alloc();
	}

	public Channel read() {
		return channel.read();
	}

	public Channel flush() {
		return channel.flush();
	}

	public ChannelFuture close() {
		return channel.close();
	}

	public ChannelFuture close(ChannelPromise promise) {
		return channel.close(promise);
	}

	/**
	 * sendTextMessage
	 * @param message
	 * @return
	 */
	public abstract ChannelFuture sendTextMessage(String message);
	
	/**
	 * sendBinaryMessage
	 * @param byteBuf
	 * @return
	 */
	public abstract ChannelFuture sendBinaryMessage(ByteBuf byteBuf);
}
