package emissary.core.channels;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import emissary.test.core.UnitTest;
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel;
import org.junit.Test;

public class InMemoryChannelFactoryTest extends UnitTest {

    @Test
    public void testCannotCreateFactoryWithNullByteArray() {
        assertThrows(NullPointerException.class, () -> InMemoryChannelFactory.create(null),
                "Factory allowed null to be set, which would fail when getting an instance later");
    }

    @Test
    public void testNormalPath() throws IOException {
        final String testString = "Test data";
        final SeekableByteChannelFactory sbcf = InMemoryChannelFactory.create(testString.getBytes());
        final ByteBuffer buff = ByteBuffer.allocate(testString.length());
        sbcf.create().read(buff);
        assertEquals(testString, new String(buff.array()));
    }

    @Test
    public void testImmutability() throws IOException {
        final SeekableByteChannelFactory sbcf = InMemoryChannelFactory.create("Test data".getBytes());
        final SeekableByteChannel sbc = sbcf.create();
        final ByteBuffer buff = ByteBuffer.wrap("New data".getBytes());
        assertThrows(NonWritableChannelException.class, () -> sbc.write(buff), "Can't write to byte channel as it's immutable");
        assertThrows(NonWritableChannelException.class, () -> sbc.truncate(5l), "Can't truncate byte channel as it's immutable");
        assertThrows(ClassCastException.class, () -> ((SeekableInMemoryByteChannel) sbc).array(),
                "Can't get different variant of SBC as we've abstracted it away");
    }

    @Test
    public void testCanCreateAndRetrieveEmptyByteArray() throws IOException {
        final SeekableByteChannelFactory simbcf = InMemoryChannelFactory.create(new byte[0]);
        assertEquals(0L, simbcf.create().size());
    }

    @Test
    public void testKryoSerialization() throws Exception {
        final Kryo kryo = new Kryo();
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final Output output = new Output(baos);
        final byte[] testBytes = "Test Bytes".getBytes(StandardCharsets.UTF_8);
        final SeekableByteChannelFactory sbcf = SeekableByteChannelHelper.memory(testBytes);
        kryo.register(byte[].class);
        kryo.register(Class.forName("emissary.core.channels.ImmutableChannelFactory$ImmutableChannelFactoryImpl"));
        kryo.register(Class.forName("emissary.core.channels.InMemoryChannelFactory$InMemoryChannelFactoryImpl"));
        kryo.writeClassAndObject(output, sbcf);
        output.close();
        final byte[] serializedBytes = baos.toByteArray();
        final Input input = new Input(serializedBytes);
        final SeekableByteChannelFactory newSbcf = (SeekableByteChannelFactory) kryo.readClassAndObject(input);
        final SeekableByteChannel sbc = newSbcf.create();
        final ByteBuffer byteBuffer = ByteBuffer.allocate((int) sbc.size());
        sbc.read(byteBuffer);
        assertArrayEquals(testBytes, byteBuffer.array(), "SeekableByteChannelFactory byte[]'s are not equal!");
    }
}
