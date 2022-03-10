package emissary.core.channels;

import java.io.IOException;
import java.nio.channels.SeekableByteChannel;

/**
 * Interface to provide a consistent experience dealing with streaming data.
 */
public interface SeekableByteChannelFactory {

    /**
     * Creates a channel from the referenced data that already exists as part of the factory's parent.
     * 
     * See {@link InMemoryChannelFactory} for an example of how to implement this.
     * 
     * @return a channel with access to the data
     * @throws IOException if an error occurs creating the instance e.g. the provided data no longer exists
     */
    SeekableByteChannel create() throws IOException;
}
