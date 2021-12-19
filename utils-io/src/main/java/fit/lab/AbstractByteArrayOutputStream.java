
package fit.lab.output;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.SequenceInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fit.lab.IOUtils;
import fit.lab.input.ClosedInputStream;


public abstract class AbstractByteArrayOutputStream extends OutputStream {

    
    @FunctionalInterface
    protected interface InputStreamConstructor<T extends InputStream> {

       
        T construct(final byte[] buf, final int offset, final int length);
    }

    static final int DEFAULT_SIZE = 1024;
    
    private final List<byte[]> buffers = new ArrayList<>();
    
    private int currentBufferIndex;
    
    private int filledBufferSum;
    
    private byte[] currentBuffer;
    
    protected int count;

    
    private boolean reuseBuffers = true;

    
    @Override
    public void close() throws IOException {
        
    }

    
    protected void needNewBuffer(final int newcount) {
        if (currentBufferIndex < buffers.size() - 1) {
           
            filledBufferSum += currentBuffer.length;

            currentBufferIndex++;
            currentBuffer = buffers.get(currentBufferIndex);
        } else {
           
            final int newBufferSize;
            if (currentBuffer == null) {
                newBufferSize = newcount;
                filledBufferSum = 0;
            } else {
                newBufferSize = Math.max(currentBuffer.length << 1, newcount - filledBufferSum);
                filledBufferSum += currentBuffer.length;
            }

            currentBufferIndex++;
            currentBuffer = IOUtils.byteArray(newBufferSize);
            buffers.add(currentBuffer);
        }
    }

   
    public abstract void reset();

    
    protected void resetImpl() {
        count = 0;
        filledBufferSum = 0;
        currentBufferIndex = 0;
        if (reuseBuffers) {
            currentBuffer = buffers.get(currentBufferIndex);
        } else {
            //Throw away old buffers
            currentBuffer = null;
            final int size = buffers.get(0).length;
            buffers.clear();
            needNewBuffer(size);
            reuseBuffers = true;
        }
    }


    public abstract int size();
   
    public abstract byte[] toByteArray();

    protected byte[] toByteArrayImpl() {
        int remaining = count;
        if (remaining == 0) {
            return IOUtils.EMPTY_BYTE_ARRAY;
        }
        final byte[] newbuf = IOUtils.byteArray(remaining);
        int pos = 0;
        for (final byte[] buf : buffers) {
            final int c = Math.min(buf.length, remaining);
            System.arraycopy(buf, 0, newbuf, pos, c);
            pos += c;
            remaining -= c;
            if (remaining == 0) {
                break;
            }
        }
        return newbuf;
    }

   
    public abstract InputStream toInputStream();

    
    @SuppressWarnings("resource") // The result InputStream MUST be managed by the call site.
    protected <T extends InputStream> InputStream toInputStream(
            final InputStreamConstructor<T> isConstructor) {
        int remaining = count;
        if (remaining == 0) {
            return ClosedInputStream.INSTANCE;
        }
        final List<T> list = new ArrayList<>(buffers.size());
        for (final byte[] buf : buffers) {
            final int c = Math.min(buf.length, remaining);
            list.add(isConstructor.construct(buf, 0, c));
            remaining -= c;
            if (remaining == 0) {
                break;
            }
        }
        reuseBuffers = false;
        return new SequenceInputStream(Collections.enumeration(list));
    }

   
    @Override
    @Deprecated
    public String toString() {
        // make explicit the use of the default charset
        return new String(toByteArray(), Charset.defaultCharset());
    }

   
    public String toString(final Charset charset) {
        return new String(toByteArray(), charset);
    }

    
    public String toString(final String enc) throws UnsupportedEncodingException {
        return new String(toByteArray(), enc);
    }

   
    @Override
    public abstract void write(final byte[] b, final int off, final int len);

    
    public abstract int write(final InputStream in) throws IOException;

    
    @Override
    public abstract void write(final int b);

    
    protected void writeImpl(final byte[] b, final int off, final int len) {
        final int newcount = count + len;
        int remaining = len;
        int inBufferPos = count - filledBufferSum;
        while (remaining > 0) {
            final int part = Math.min(remaining, currentBuffer.length - inBufferPos);
            System.arraycopy(b, off + len - remaining, currentBuffer, inBufferPos, part);
            remaining -= part;
            if (remaining > 0) {
                needNewBuffer(newcount);
                inBufferPos = 0;
            }
        }
        count = newcount;
    }

    
    protected int writeImpl(final InputStream in) throws IOException {
        int readCount = 0;
        int inBufferPos = count - filledBufferSum;
        int n = in.read(currentBuffer, inBufferPos, currentBuffer.length - inBufferPos);
        while (n != IOUtils.EOF) {
            readCount += n;
            inBufferPos += n;
            count += n;
            if (inBufferPos == currentBuffer.length) {
                needNewBuffer(currentBuffer.length);
                inBufferPos = 0;
            }
            n = in.read(currentBuffer, inBufferPos, currentBuffer.length - inBufferPos);
        }
        return readCount;
    }

    protected void writeImpl(final int b) {
        int inBufferPos = count - filledBufferSum;
        if (inBufferPos == currentBuffer.length) {
            needNewBuffer(count + 1);
            inBufferPos = 0;
        }
        currentBuffer[inBufferPos] = (byte) b;
        count++;
    }

    
    public abstract void writeTo(final OutputStream out) throws IOException;

    
    protected void writeToImpl(final OutputStream out) throws IOException {
        int remaining = count;
        for (final byte[] buf : buffers) {
            final int c = Math.min(buf.length, remaining);
            out.write(buf, 0, c);
            remaining -= c;
            if (remaining == 0) {
                break;
            }
        }
    }

}
