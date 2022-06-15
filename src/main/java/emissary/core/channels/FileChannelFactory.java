package emissary.core.channels;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.util.Set;

import org.apache.commons.lang3.Validate;

/**
 * Provide a file-backed implementation for streaming data to a consumer
 */
public final class FileChannelFactory {
    private FileChannelFactory() {}

    /**
     * <p>
     * Create a new instance of the factory using the provided file reference. Ultimately, wraps a standard FileChannel
     * with immutability
     * </p>
     * 
     * <p>
     * If you just want to simply open a file for reading, see {@link SeekableByteChannelHelper#FILE_READ_ONLY}
     * </p>
     * 
     * @param path containing a reference to the file
     * @param options to use when opening the file
     * @return a new instance
     * @see SeekableByteChannelHelper#FILE_READ_ONLY
     * @see SeekableByteChannelHelper#file(Path, Set)
     */
    public static SeekableByteChannelFactory create(final Path path, final Set<? extends OpenOption> options) {
        return ImmutableChannelFactory.create(new FileChannelFactoryImpl(path, options));
    }

    /**
     * Private class to hide implementation details from callers
     */
    private static final class FileChannelFactoryImpl implements SeekableByteChannelFactory {
        private final Path path;
        private final Set<? extends OpenOption> options;

        private FileChannelFactoryImpl(final Path path, final Set<? extends OpenOption> options) {
            Validate.notNull(path, "Required: file not null");
            Validate.notNull(options, "Required: options not null");
            this.path = path;
            this.options = options;
        }

        /**
         * Creates a {@link FileChannel} instance with the configured options to the configured file.
         * 
         * @return the new channel instance
         */
        @Override
        public SeekableByteChannel create() throws IOException {
            return FileChannel.open(path, options);
        }
    }
}
