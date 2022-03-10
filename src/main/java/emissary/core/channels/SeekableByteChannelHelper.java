package emissary.core.channels;

/**
 * Helper methods to handle {@link java.nio.channels.SeekableByteChannel} objects
 */
public final class SeekableByteChannelHelper {
    private SeekableByteChannelHelper() {}

    /**
     * Make an existing factory immutable.
     * 
     * @param sbcf to make immutable
     * @return the wrapped factory
     */
    public static SeekableByteChannelFactory immutable(final SeekableByteChannelFactory sbcf) {
        return ImmutableChannelFactory.create(sbcf);
    }

    /**
     * Create an in memory SBC factory which can be used to create any number of channels based on the provided bytes
     * without storing them multiple times.
     * 
     * @param bytes to use with the channel
     * @return the factory
     */
    public static SeekableByteChannelFactory memory(final byte[] bytes) {
        return InMemoryChannelFactory.create(bytes);
    }
}
