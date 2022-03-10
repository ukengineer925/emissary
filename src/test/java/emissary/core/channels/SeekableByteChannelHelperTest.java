package emissary.core.channels;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;

import emissary.test.core.UnitTest;
import org.junit.Test;

public class SeekableByteChannelHelperTest extends UnitTest {

    public static final String TEST_STRING = "test data";

    @Test
    public void testImmutable() throws IOException {
        final SeekableByteChannelFactory sbcf = SeekableByteChannelHelper.immutable(SeekableByteChannelHelper.memory(TEST_STRING.getBytes()));
        final SeekableByteChannel sbc = sbcf.create();
        final ByteBuffer someTestBytes = ByteBuffer.wrap(TEST_STRING.getBytes());
        assertThrows(NonWritableChannelException.class, () -> sbc.truncate(2l));
        assertThrows(NonWritableChannelException.class, () -> sbc.write(someTestBytes));
    }

    @Test
    public void testMemory() throws IOException {
        final SeekableByteChannelFactory sbcf = SeekableByteChannelHelper.memory(TEST_STRING.getBytes());
        final SeekableByteChannel sbc = sbcf.create();
        final ByteBuffer buff = ByteBuffer.allocate(4);
        sbc.read(buff);
        buff.flip();
        byte[] bytes = new byte[4];
        buff.get(bytes);
        assertArrayEquals("test".getBytes(), bytes);
    }
}
