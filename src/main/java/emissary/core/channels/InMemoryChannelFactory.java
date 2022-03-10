package emissary.core.channels;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.apache.commons.lang3.Validate;

/**
 * Provide an in-memory backed implementation for streaming data to a consumer
 */
public final class InMemoryChannelFactory {
    private InMemoryChannelFactory() {}

    /**
     * Create a new instance of the factory using the provided byte array
     * 
     * @param bytes containing the data to provide to consumers in an immutable manner
     * @return a new instance
     */
    public static SeekableByteChannelFactory create(final byte[] bytes) {
        return ImmutableChannelFactory.create(new InMemoryChannelFactoryImpl(bytes));
    }

    /**
     * Private class to hide implementation details from callers
     */
    private static final class InMemoryChannelFactoryImpl implements SeekableByteChannelFactory {
        private final byte[] bytes;

        // Private constructor for Kryo serialisation
        private InMemoryChannelFactoryImpl() {
            bytes = null;
        }

        private InMemoryChannelFactoryImpl(final byte[] bytes) {
            Validate.notNull(bytes, "Required: bytes not null");
            this.bytes = bytes;
        }

        /**
         * Create an immutable byte channel to the existing byte array (no copy in/out regardless of how many channels are
         * created)
         * 
         * @return the new channel instance
         */
        @Override
        public SeekableByteChannel create() throws IOException {
            return new SeekableInMemoryByteChannel(bytes);
        }
    }
}
