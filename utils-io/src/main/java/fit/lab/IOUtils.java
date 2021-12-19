package fit.lab;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.CharArrayWriter;
import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.Selector;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

import fit.lab.function.IOConsumer;
import fit.lab.input.QueueInputStream;
import fit.lab.output.AppendableWriter;
import fit.lab.output.ByteArrayOutputStream;
import fit.lab.output.NullOutputStream;
import fit.lab.output.StringBuilderWriter;
import fit.lab.output.ThresholdingOutputStream;
import fit.lab.output.UnsynchronizedByteArrayOutputStream;


public class IOUtils {
   
    public static final int CR = '\r';

   
    public static final int DEFAULT_BUFFER_SIZE = 8192;

  
    public static final char DIR_SEPARATOR = File.separatorChar;

  
    public static final char DIR_SEPARATOR_UNIX = '/';


    public static final char DIR_SEPARATOR_WINDOWS = '\\';

 
    public static final byte[] EMPTY_BYTE_ARRAY = {};

  
    public static final int EOF = -1;

  
    public static final int LF = '\n';

  
    @Deprecated
    public static final String LINE_SEPARATOR = System.lineSeparator();

    public static final String LINE_SEPARATOR_UNIX = StandardLineSeparator.LF.getString();

   
    public static final String LINE_SEPARATOR_WINDOWS = StandardLineSeparator.CRLF.getString();

    private static final ThreadLocal<byte[]> SKIP_BYTE_BUFFER = ThreadLocal.withInitial(IOUtils::byteArray);

 
    private static final ThreadLocal<char[]> SKIP_CHAR_BUFFER = ThreadLocal.withInitial(IOUtils::charArray);


    @SuppressWarnings("resource") 
    public static BufferedInputStream buffer(final InputStream inputStream) {
       
        Objects.requireNonNull(inputStream, "inputStream");
        return inputStream instanceof BufferedInputStream ?
                (BufferedInputStream) inputStream : new BufferedInputStream(inputStream);
    }


    @SuppressWarnings("resource") 
    public static BufferedInputStream buffer(final InputStream inputStream, final int size) {
        
        Objects.requireNonNull(inputStream, "inputStream");
        return inputStream instanceof BufferedInputStream ?
                (BufferedInputStream) inputStream : new BufferedInputStream(inputStream, size);
    }

   
    @SuppressWarnings("resource") 
    public static BufferedOutputStream buffer(final OutputStream outputStream) {
        
        Objects.requireNonNull(outputStream, "outputStream");
        return outputStream instanceof BufferedOutputStream ?
                (BufferedOutputStream) outputStream : new BufferedOutputStream(outputStream);
    }

    @SuppressWarnings("resource") 
    pub
        Objects.requireNonNull(outputStream, "outputStream");
        return outputStream instanceof BufferedOutputStream ?
                (BufferedOutputStream) outputStream : new BufferedOutputStream(outputStream, size);
    }

    public static BufferedReader buffer(final Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

 
    public static BufferedReader buffer(final Reader reader, final int size) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader, size);
    }

  
    public static BufferedWriter buffer(final Writer writer) {
        return writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer);
    }


    public static BufferedWriter buffer(final Writer writer, final int size) {
        return writer instanceof BufferedWriter ? (BufferedWriter) writer : new BufferedWriter(writer, size);
    }

 
    public static byte[] byteArray() {
        return byteArray(DEFAULT_BUFFER_SIZE);
    }

  
    public static byte[] byteArray(final int size) {
        return new byte[size];
    }


    private static char[] charArray() {
        return charArray(DEFAULT_BUFFER_SIZE);
    }


    private static char[] charArray(final int size) {
        return new char[size];
    }

 
    public static void close(final Closeable closeable) throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }

 
    public static void close(final Closeable... closeables) throws IOException {
        IOConsumer.forEach(closeables, IOUtils::close);
    }


    public static void close(final Closeable closeable, final IOConsumer<IOException> consumer) throws IOException {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final IOException e) {
                if (consumer != null) {
                    consumer.accept(e);
                }
            }
        }
    }

 
    public static void close(final URLConnection conn) {
        if (conn instanceof HttpURLConnection) {
            ((HttpURLConnection) conn).disconnect();
        }
    }

   
    public static void closeQuietly(final Closeable closeable) {
        closeQuietly(closeable, null);
    }

    
    public static void closeQuietly(final Closeable... closeables) {
        if (closeables != null) {
            Arrays.stream(closeables).forEach(IOUtils::closeQuietly);
        }
    }

 
    public static void closeQuietly(final Closeable closeable, final Consumer<IOException> consumer) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (final IOException e) {
                if (consumer != null) {
                    consumer.accept(e);
                }
            }
        }
    }

    
    public static void closeQuietly(final InputStream input) {
        closeQuietly((Closeable) input);
    }

    public static void closeQuietly(final OutputStream output) {
        closeQuietly((Closeable) output);
    }

    public static void closeQuietly(final Reader reader) {
        closeQuietly((Closeable) reader);
    }

    public static void closeQuietly(final Selector selector) {
        closeQuietly((Closeable) selector);
    }

    public static void closeQuietly(final ServerSocket serverSocket) {
        closeQuietly((Closeable) serverSocket);
    }

   
    public static void closeQuietly(final Socket socket) {
        closeQuietly((Closeable) socket);
    }

  
    public static void closeQuietly(final Writer writer) {
        closeQuietly((Closeable) writer);
    }

    public static long consume(final InputStream input)
            throws IOException {
        return copyLarge(input, NullOutputStream.INSTANCE, getByteArray());
    }

    public static boolean contentEquals(final InputStream input1, final InputStream input2) throws IOException {
       
        if (input1 == input2) {
            return true;
        }
        if (input1 == null || input2 == null) {
            return false;
        }

        final byte[] array1 = getByteArray();
        final byte[] array2 = byteArray();
        int pos1;
        int pos2;
        int count1;
        int count2;
        while (true) {
            pos1 = 0;
            pos2 = 0;
            for (int index = 0; index < DEFAULT_BUFFER_SIZE; index++) {
                if (pos1 == index) {
                    do {
                        count1 = input1.read(array1, pos1, DEFAULT_BUFFER_SIZE - pos1);
                    } while (count1 == 0);
                    if (count1 == EOF) {
                        return pos2 == index && input2.read() == EOF;
                    }
                    pos1 += count1;
                }
                if (pos2 == index) {
                    do {
                        count2 = input2.read(array2, pos2, DEFAULT_BUFFER_SIZE - pos2);
                    } while (count2 == 0);
                    if (count2 == EOF) {
                        return pos1 == index && input1.read() == EOF;
                    }
                    pos2 += count2;
                }
                if (array1[index] != array2[index]) {
                    return false;
                }
            }
        }
    }


    public static boolean contentEquals(final Reader input1, final Reader input2) throws IOException {
        if (input1 == input2) {
            return true;
        }
        if (input1 == null || input2 == null) {
            return false;
        }

        final char[] array1 = getCharArray();
        final char[] array2 = charArray();
        int pos1;
        int pos2;
        int count1;
        int count2;
        while (true) {
            pos1 = 0;
            pos2 = 0;
            for (int index = 0; index < DEFAULT_BUFFER_SIZE; index++) {
                if (pos1 == index) {
                    do {
                        count1 = input1.read(array1, pos1, DEFAULT_BUFFER_SIZE - pos1);
                    } while (count1 == 0);
                    if (count1 == EOF) {
                        return pos2 == index && input2.read() == EOF;
                    }
                    pos1 += count1;
                }
                if (pos2 == index) {
                    do {
                        count2 = input2.read(array2, pos2, DEFAULT_BUFFER_SIZE - pos2);
                    } while (count2 == 0);
                    if (count2 == EOF) {
                        return pos1 == index && input1.read() == EOF;
                    }
                    pos2 += count2;
                }
                if (array1[index] != array2[index]) {
                    return false;
                }
            }
        }
    }

    @SuppressWarnings("resource")
    public static boolean contentEqualsIgnoreEOL(final Reader reader1, final Reader reader2)
            throws IOException {
        if (reader1 == reader2) {
            return true;
        }
        if (reader1 == null ^ reader2 == null) {
            return false;
        }
        final BufferedReader br1 = toBufferedReader(reader1);
        final BufferedReader br2 = toBufferedReader(reader2);

        String line1 = br1.readLine();
        String line2 = br2.readLine();
        while (line1 != null && line1.equals(line2)) {
            line1 = br1.readLine();
            line2 = br2.readLine();
        }
        return Objects.equals(line1, line2);
    }

 
    public static int copy(final InputStream inputStream, final OutputStream outputStream) throws IOException {
        final long count = copyLarge(inputStream, outputStream);
        if (count > Integer.MAX_VALUE) {
            return EOF;
        }
        return (int) count;
    }

 
    public static long copy(final InputStream inputStream, final OutputStream outputStream, final int bufferSize)
            throws IOException {
        return copyLarge(inputStream, outputStream, IOUtils.byteArray(bufferSize));
    }

    
    @Deprecated
    public static void copy(final InputStream input, final Writer writer)
            throws IOException {
        copy(input, writer, Charset.defaultCharset());
    }

 
    public static void copy(final InputStream input, final Writer writer, final Charset inputCharset)
            throws IOException {
        final InputStreamReader reader = new InputStreamReader(input, Charsets.toCharset(inputCharset));
        copy(reader, writer);
    }

    public static void copy(final InputStream input, final Writer writer, final String inputCharsetName)
            throws IOException {
        copy(input, writer, Charsets.toCharset(inputCharsetName));
    }

   
    @SuppressWarnings("resource")
    public static QueueInputStream copy(final java.io.ByteArrayOutputStream outputStream) throws IOException {
        Objects.requireNonNull(outputStream, "outputStream");
        final QueueInputStream in = new QueueInputStream();
        outputStream.writeTo(in.newQueueOutputStream());
        return in;
    }

  
    public static long copy(final Reader reader, final Appendable output) throws IOException {
        return copy(reader, output, CharBuffer.allocate(DEFAULT_BUFFER_SIZE));
    }


    public static long copy(final Reader reader, final Appendable output, final CharBuffer buffer) throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = reader.read(buffer))) {
            buffer.flip();
            output.append(buffer, 0, n);
            count += n;
        }
        return count;
    }

   
    @Deprecated
    public static void copy(final Reader reader, final OutputStream output)
            throws IOException {
        copy(reader, output, Charset.defaultCharset());
    }

  
    public static void copy(final Reader reader, final OutputStream output, final Charset outputCharset)
            throws IOException {
        final OutputStreamWriter writer = new OutputStreamWriter(output, Charsets.toCharset(outputCharset));
        copy(reader, writer);
        
        writer.flush();
    }


    public static void copy(final Reader reader, final OutputStream output, final String outputCharsetName)
            throws IOException {
        copy(reader, output, Charsets.toCharset(outputCharsetName));
    }

    
    public static int copy(final Reader reader, final Writer writer) throws IOException {
        final long count = copyLarge(reader, writer);
        if (count > Integer.MAX_VALUE) {
            return EOF;
        }
        return (int) count;
    }

    public static long copy(final URL url, final File file) throws IOException {
        try (OutputStream outputStream = Files.newOutputStream(Objects.requireNonNull(file, "file").toPath())) {
            return copy(url, outputStream);
        }
    }

    public static long copy(final URL url, final OutputStream outputStream) throws IOException {
        try (InputStream inputStream = Objects.requireNonNull(url, "url").openStream()) {
            return copyLarge(inputStream, outputStream);
        }
    }

   
    public static long copyLarge(final InputStream inputStream, final OutputStream outputStream)
            throws IOException {
        return copy(inputStream, outputStream, DEFAULT_BUFFER_SIZE);
    }


    @SuppressWarnings("resource") 
    public static long copyLarge(final InputStream inputStream, final OutputStream outputStream, final byte[] buffer)
        throws IOException {
        Objects.requireNonNull(inputStream, "inputStream");
        Objects.requireNonNull(outputStream, "outputStream");
        long count = 0;
        int n;
        while (EOF != (n = inputStream.read(buffer))) {
            outputStream.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

  
    public static long copyLarge(final InputStream input, final OutputStream output, final long inputOffset,
                                 final long length) throws IOException {
        return copyLarge(input, output, inputOffset, length, getByteArray());
    }

 
    public static long copyLarge(final InputStream input, final OutputStream output,
                                 final long inputOffset, final long length, final byte[] buffer) throws IOException {
        if (inputOffset > 0) {
            skipFully(input, inputOffset);
        }
        if (length == 0) {
            return 0;
        }
        final int bufferLength = buffer.length;
        int bytesToRead = bufferLength;
        if (length > 0 && length < bufferLength) {
            bytesToRead = (int) length;
        }
        int read;
        long totalRead = 0;
        while (bytesToRead > 0 && EOF != (read = input.read(buffer, 0, bytesToRead))) {
            output.write(buffer, 0, read);
            totalRead += read;
            if (length > 0) {
                bytesToRead = (int) Math.min(length - totalRead, bufferLength);
            }
        }
        return totalRead;
    }

   
    public static long copyLarge(final Reader reader, final Writer writer) throws IOException {
        return copyLarge(reader, writer, getCharArray());
    }

   
    public static long copyLarge(final Reader reader, final Writer writer, final char[] buffer) throws IOException {
        long count = 0;
        int n;
        while (EOF != (n = reader.read(buffer))) {
            writer.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

 
    public static long copyLarge(final Reader reader, final Writer writer, final long inputOffset, final long length)
            throws IOException {
        return copyLarge(reader, writer, inputOffset, length, getCharArray());
    }

    
    public static long copyLarge(final Reader reader, final Writer writer, final long inputOffset, final long length,
                                 final char[] buffer)
            throws IOException {
        if (inputOffset > 0) {
            skipFully(reader, inputOffset);
        }
        if (length == 0) {
            return 0;
        }
        int bytesToRead = buffer.length;
        if (length > 0 && length < buffer.length) {
            bytesToRead = (int) length;
        }
        int read;
        long totalRead = 0;
        while (bytesToRead > 0 && EOF != (read = reader.read(buffer, 0, bytesToRead))) {
            writer.write(buffer, 0, read);
            totalRead += read;
            if (length > 0) { 
                bytesToRead = (int) Math.min(length - totalRead, buffer.length);
            }
        }
        return totalRead;
    }

    static byte[] getByteArray() {
        return SKIP_BYTE_BUFFER.get();
    }


    static char[] getCharArray() {
        return SKIP_CHAR_BUFFER.get();
    }

    public static int length(final byte[] array) {
        return array == null ? 0 : array.length;
    }

    public static int length(final char[] array) {
        return array == null ? 0 : array.length;
    }


    public static int length(final CharSequence csq) {
        return csq == null ? 0 : csq.length();
    }

    public static int length(final Object[] array) {
        return array == null ? 0 : array.length;
    }

    
    public static LineIterator lineIterator(final InputStream input, final Charset charset) {
        return new LineIterator(new InputStreamReader(input, Charsets.toCharset(charset)));
    }

    
    
    public static LineIterator lineIterator(final InputStream input, final String charsetName) {
        return lineIterator(input, Charsets.toCharset(charsetName));
    }

   
    public static LineIterator lineIterator(final Reader reader) {
        return new LineIterator(reader);
    }

    
    public static int read(final InputStream input, final byte[] buffer) throws IOException {
        return read(input, buffer, 0, buffer.length);
    }

    
    public static int read(final InputStream input, final byte[] buffer, final int offset, final int length)
            throws IOException {
        if (length < 0) {
            throw new IllegalArgumentException("Length must not be negative: " + length);
        }
        int remaining = length;
        while (remaining > 0) {
            final int location = length - remaining;
            final int count = input.read(buffer, offset + location, remaining);
            if (EOF == count) {
                break;
            }
            remaining -= count;
        }
        return length - remaining;
    }

  
    public static int read(final ReadableByteChannel input, final ByteBuffer buffer) throws IOException {
        final int length = buffer.remaining();
        while (buffer.remaining() > 0) {
            final int count = input.read(buffer);
            if (EOF == count) { 
                break;
            }
        }
        return length - buffer.remaining();
    }

   
    public static int read(final Reader reader, final char[] buffer) throws IOException {
        return read(reader, buffer, 0, buffer.length);
    }

    
    public static int read(final Reader reader, final char[] buffer, final int offset, final int length)
            throws IOException {
        if (length < 0) {
            throw new IllegalArgumentException("Length must not be negative: " + length);
        }
        int remaining = length;
        while (remaining > 0) {
            final int location = length - remaining;
            final int count = reader.read(buffer, offset + location, remaining);
            if (EOF == count) { 
                break;
            }
            remaining -= count;
        }
        return length - remaining;
    }

   
    public static void readFully(final InputStream input, final byte[] buffer) throws IOException {
        readFully(input, buffer, 0, buffer.length);
    }

  
    public static void readFully(final InputStream input, final byte[] buffer, final int offset, final int length)
            throws IOException {
        final int actual = read(input, buffer, offset, length);
        if (actual != length) {
            throw new EOFException("Length to read: " + length + " actual: " + actual);
        }
    }

  
    public static byte[] readFully(final InputStream input, final int length) throws IOException {
        final byte[] buffer = IOUtils.byteArray(length);
        readFully(input, buffer, 0, buffer.length);
        return buffer;
    }

 
    public static void readFully(final ReadableByteChannel input, final ByteBuffer buffer) throws IOException {
        final int expected = buffer.remaining();
        final int actual = read(input, buffer);
        if (actual != expected) {
            throw new EOFException("Length to read: " + expected + " actual: " + actual);
        }
    }


    public static void readFully(final Reader reader, final char[] buffer) throws IOException {
        readFully(reader, buffer, 0, buffer.length);
    }

  
    public static void readFully(final Reader reader, final char[] buffer, final int offset, final int length)
            throws IOException {
        final int actual = read(reader, buffer, offset, length);
        if (actual != length) {
            throw new EOFException("Length to read: " + length + " actual: " + actual);
        }
    }

   
    @Deprecated
    public static List<String> readLines(final InputStream input) throws IOException {
        return readLines(input, Charset.defaultCharset());
    }

    
    public static List<String> readLines(final InputStream input, final Charset charset) throws IOException {
        final InputStreamReader reader = new InputStreamReader(input, Charsets.toCharset(charset));
        return readLines(reader);
    }

    public static List<String> readLines(final InputStream input, final String charsetName) throws IOException {
        return readLines(input, Charsets.toCharset(charsetName));
    }

  
    @SuppressWarnings("resource") 
    public static List<String> readLines(final Reader reader) throws IOException {
        final BufferedReader bufReader = toBufferedReader(reader);
        final List<String> list = new ArrayList<>();
        String line;
        while ((line = bufReader.readLine()) != null) {
            list.add(line);
        }
        return list;
    }

  
    public static byte[] resourceToByteArray(final String name) throws IOException {
        return resourceToByteArray(name, null);
    }

  
    public static byte[] resourceToByteArray(final String name, final ClassLoader classLoader) throws IOException {
        return toByteArray(resourceToURL(name, classLoader));
    }

    
    public static String resourceToString(final String name, final Charset charset) throws IOException {
        return resourceToString(name, charset, null);
    }

    
    public static String resourceToString(final String name, final Charset charset, final ClassLoader classLoader) throws IOException {
        return toString(resourceToURL(name, classLoader), charset);
    }

    
    public static URL resourceToURL(final String name) throws IOException {
        return resourceToURL(name, null);
    }

    public static URL resourceToURL(final String name, final ClassLoader classLoader) throws IOException {
       
        final URL resource = classLoader == null ? IOUtils.class.getResource(name) : classLoader.getResource(name);
        if (resource == null) {
            throw new IOException("Resource not found: " + name);
        }
        return resource;
    }

    public static long skip(final InputStream input, final long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        }
      
        long remain = toSkip;
        while (remain > 0) {
            final byte[] byteArray = getByteArray();
            final long n = input.read(byteArray, 0, (int) Math.min(remain, byteArray.length));
            if (n < 0) { 
                break;
            }
            remain -= n;
        }
        return toSkip - remain;
    }

    
    public static long skip(final ReadableByteChannel input, final long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        }
        final ByteBuffer skipByteBuffer = ByteBuffer.allocate((int) Math.min(toSkip, DEFAULT_BUFFER_SIZE));
        long remain = toSkip;
        while (remain > 0) {
            skipByteBuffer.position(0);
            skipByteBuffer.limit((int) Math.min(remain, DEFAULT_BUFFER_SIZE));
            final int n = input.read(skipByteBuffer);
            if (n == EOF) {
                break;
            }
            remain -= n;
        }
        return toSkip - remain;
    }

    
    public static long skip(final Reader reader, final long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Skip count must be non-negative, actual: " + toSkip);
        }
        long remain = toSkip;
        while (remain > 0) {
            final char[] charArray = getCharArray();
            final long n = reader.read(charArray, 0, (int) Math.min(remain, charArray.length));
            if (n < 0) { 
                break;
            }
            remain -= n;
        }
        return toSkip - remain;
    }

    
    public static void skipFully(final InputStream input, final long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip);
        }
        final long skipped = skip(input, toSkip);
        if (skipped != toSkip) {
            throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped);
        }
    }

    
    public static void skipFully(final ReadableByteChannel input, final long toSkip) throws IOException {
        if (toSkip < 0) {
            throw new IllegalArgumentException("Bytes to skip must not be negative: " + toSkip);
        }
        final long skipped = skip(input, toSkip);
        if (skipped != toSkip) {
            throw new EOFException("Bytes to skip: " + toSkip + " actual: " + skipped);
        }
    }

    
    public static void skipFully(final Reader reader, final long toSkip) throws IOException {
        final long skipped = skip(reader, toSkip);
        if (skipped != toSkip) {
            throw new EOFException("Chars to skip: " + toSkip + " actual: " + skipped);
        }
    }

   
    public static InputStream toBufferedInputStream(final InputStream input) throws IOException {
        return ByteArrayOutputStream.toBufferedInputStream(input);
    }

    
    public static InputStream toBufferedInputStream(final InputStream input, final int size) throws IOException {
        return ByteArrayOutputStream.toBufferedInputStream(input, size);
    }

   
    public static BufferedReader toBufferedReader(final Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
    }

    public static BufferedReader toBufferedReader(final Reader reader, final int size) {
        return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader, size);
    }

    
    public static byte[] toByteArray(final InputStream inputStream) throws IOException {
        try (final UnsynchronizedByteArrayOutputStream ubaOutput = new UnsynchronizedByteArrayOutputStream();
            final ThresholdingOutputStream thresholdOuput = new ThresholdingOutputStream(Integer.MAX_VALUE, os -> {
                throw new IllegalArgumentException(
                    String.format("Cannot read more than %,d into a byte array", Integer.MAX_VALUE));
            }, os -> ubaOutput)) {
            copy(inputStream, thresholdOuput);
            return ubaOutput.toByteArray();
        }
    }

    public static byte[] toByteArray(final InputStream input, final int size) throws IOException {

        if (size < 0) {
            throw new IllegalArgumentException("Size must be equal or greater than zero: " + size);
        }

        if (size == 0) {
            return EMPTY_BYTE_ARRAY;
        }

        final byte[] data = IOUtils.byteArray(size);
        int offset = 0;
        int read;

        while (offset < size && (read = input.read(data, offset, size - offset)) != EOF) {
            offset += read;
        }

        if (offset != size) {
            throw new IOException("Unexpected read size, current: " + offset + ", expected: " + size);
        }

        return data;
    }

 
    public static byte[] toByteArray(final InputStream input, final long size) throws IOException {
        if (size > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Size cannot be greater than Integer max value: " + size);
        }
        return toByteArray(input, (int) size);
    }


    @Deprecated
    public static byte[] toByteArray(final Reader reader) throws IOException {
        return toByteArray(reader, Charset.defaultCharset());
    }

    public static byte[] toByteArray(final Reader reader, final Charset charset) throws IOException {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            copy(reader, output, charset);
            return output.toByteArray();
        }
    }

 
    public static byte[] toByteArray(final Reader reader, final String charsetName) throws IOException {
        return toByteArray(reader, Charsets.toCharset(charsetName));
    }

   
    @Deprecated
    public static byte[] toByteArray(final String input) {
        return input.getBytes(Charset.defaultCharset());
    }

    
    public static byte[] toByteArray(final URI uri) throws IOException {
        return IOUtils.toByteArray(uri.toURL());
    }

    
    public static byte[] toByteArray(final URL url) throws IOException {
        try (final CloseableURLConnection urlConnection = CloseableURLConnection.open(url)) {
            return IOUtils.toByteArray(urlConnection);
        }
    }

    
    public static byte[] toByteArray(final URLConnection urlConnection) throws IOException {
        try (InputStream inputStream = urlConnection.getInputStream()) {
            return IOUtils.toByteArray(inputStream);
        }
    }

    @Deprecated
    public static char[] toCharArray(final InputStream inputStream) throws IOException {
        return toCharArray(inputStream, Charset.defaultCharset());
    }

   
    public static char[] toCharArray(final InputStream inputStream, final Charset charset)
            throws IOException {
        final CharArrayWriter writer = new CharArrayWriter();
        copy(inputStream, writer, charset);
        return writer.toCharArray();
    }

    public static char[] toCharArray(final InputStream inputStream, final String charsetName) throws IOException {
        return toCharArray(inputStream, Charsets.toCharset(charsetName));
    }

    
    public static char[] toCharArray(final Reader reader) throws IOException {
        final CharArrayWriter sw = new CharArrayWriter();
        copy(reader, sw);
        return sw.toCharArray();
    }

  
    @Deprecated
    public static InputStream toInputStream(final CharSequence input) {
        return toInputStream(input, Charset.defaultCharset());
    }

    public static InputStream toInputStream(final CharSequence input, final Charset charset) {
        return toInputStream(input.toString(), charset);
    }

    
    public static InputStream toInputStream(final CharSequence input, final String charsetName) {
        return toInputStream(input, Charsets.toCharset(charsetName));
    }

    public static InputStream toInputStream(final String input, final Charset charset) {
        return new ByteArrayInputStream(input.getBytes(Charsets.toCharset(charset)));
    }

    
    public static InputStream toInputStream(final String input, final String charsetName) {
        return new ByteArrayInputStream(input.getBytes(Charsets.toCharset(charsetName)));
    }

    @Deprecated
    public static String toString(final byte[] input) {
        return new String(input, Charset.defaultCharset());
    }

   
    @Deprecated
    public static String toString(final InputStream input) throws IOException {
        return toString(input, Charset.defaultCharset());
    }

    
    public static String toString(final InputStream input, final Charset charset) throws IOException {
        try (final StringBuilderWriter sw = new StringBuilderWriter()) {
            copy(input, sw, charset);
            return sw.toString();
        }
    }

   
    public static String toString(final InputStream input, final String charsetName)
            throws IOException {
        return toString(input, Charsets.toCharset(charsetName));
    }

   
    public static String toString(final Reader reader) throws IOException {
        try (final StringBuilderWriter sw = new StringBuilderWriter()) {
            copy(reader, sw);
            return sw.toString();
        }
    }

   
    @Deprecated
    public static String toString(final URI uri) throws IOException {
        return toString(uri, Charset.defaultCharset());
    }

    public static String toString(final URI uri, final Charset encoding) throws IOException {
        return toString(uri.toURL(), Charsets.toCharset(encoding));
    }

   
    public static String toString(final URI uri, final String charsetName) throws IOException {
        return toString(uri, Charsets.toCharset(charsetName));
    }

 
    @Deprecated
    public static String toString(final URL url) throws IOException {
        return toString(url, Charset.defaultCharset());
    }

    public static String toString(final URL url, final Charset encoding) throws IOException {
        try (InputStream inputStream = url.openStream()) {
            return toString(inputStream, encoding);
        }
    }


    public static String toString(final URL url, final String charsetName) throws IOException {
        return toString(url, Charsets.toCharset(charsetName));
    }

    public static void write(final byte[] data, final OutputStream output)
            throws IOException {
        if (data != null) {
            output.write(data);
        }
    }

   
    @Deprecated
    public static void write(final byte[] data, final Writer writer) throws IOException {
        write(data, writer, Charset.defaultCharset());
    }


    public static void write(final byte[] data, final Writer writer, final Charset charset) throws IOException {
        if (data != null) {
            writer.write(new String(data, Charsets.toCharset(charset)));
        }
    }

    public static void write(final byte[] data, final Writer writer, final String charsetName) throws IOException {
        write(data, writer, Charsets.toCharset(charsetName));
    }

    @Deprecated
    public static void write(final char[] data, final OutputStream output)
            throws IOException {
        write(data, output, Charset.defaultCharset());
    }

    public static void write(final char[] data, final OutputStream output, final Charset charset) throws IOException {
        if (data != null) {
            output.write(new String(data).getBytes(Charsets.toCharset(charset)));
        }
    }

   
    public static void write(final char[] data, final OutputStream output, final String charsetName)
            throws IOException {
        write(data, output, Charsets.toCharset(charsetName));
    }

  
    public static void write(final char[] data, final Writer writer) throws IOException {
        if (data != null) {
            writer.write(data);
        }
    }


    @Deprecated
    public static void write(final CharSequence data, final OutputStream output)
            throws IOException {
        write(data, output, Charset.defaultCharset());
    }


    public static void write(final CharSequence data, final OutputStream output, final Charset charset)
            throws IOException {
        if (data != null) {
            write(data.toString(), output, charset);
        }
    }

  
    public static void write(final CharSequence data, final OutputStream output, final String charsetName)
            throws IOException {
        write(data, output, Charsets.toCharset(charsetName));
    }

    public static void write(final CharSequence data, final Writer writer) throws IOException {
        if (data != null) {
            write(data.toString(), writer);
        }
    }


 
    @Deprecated
    public static void write(final String data, final OutputStream output)
            throws IOException {
        write(data, output, Charset.defaultCharset());
    }

   
    public static void write(final String data, final OutputStream output, final Charset charset) throws IOException {
        if (data != null) {
            output.write(data.getBytes(Charsets.toCharset(charset)));
        }
    }

  
    public static void write(final String data, final OutputStream output, final String charsetName)
            throws IOException {
        write(data, output, Charsets.toCharset(charsetName));
    }


    public static void write(final String data, final Writer writer) throws IOException {
        if (data != null) {
            writer.write(data);
        }
    }

    @Deprecated
    public static void write(final StringBuffer data, final OutputStream output) //NOSONAR
            throws IOException {
        write(data, output, (String) null);
    }

    
    @Deprecated
    public static void write(final StringBuffer data, final OutputStream output, final String charsetName) //NOSONAR
            throws IOException {
        if (data != null) {
            output.write(data.toString().getBytes(Charsets.toCharset(charsetName)));
        }
    }

   
    @Deprecated
    public static void write(final StringBuffer data, final Writer writer) 
            throws IOException {
        if (data != null) {
            writer.write(data.toString());
        }
    }

   
    public static void writeChunked(final byte[] data, final OutputStream output)
            throws IOException {
        if (data != null) {
            int bytes = data.length;
            int offset = 0;
            while (bytes > 0) {
                final int chunk = Math.min(bytes, DEFAULT_BUFFER_SIZE);
                output.write(data, offset, chunk);
                bytes -= chunk;
                offset += chunk;
            }
        }
    }

  
    public static void writeChunked(final char[] data, final Writer writer) throws IOException {
        if (data != null) {
            int bytes = data.length;
            int offset = 0;
            while (bytes > 0) {
                final int chunk = Math.min(bytes, DEFAULT_BUFFER_SIZE);
                writer.write(data, offset, chunk);
                bytes -= chunk;
                offset += chunk;
            }
        }
    }

    @Deprecated
    public static void writeLines(final Collection<?> lines, final String lineEnding,
                                  final OutputStream output) throws IOException {
        writeLines(lines, lineEnding, output, Charset.defaultCharset());
    }


    public static void writeLines(final Collection<?> lines, String lineEnding, final OutputStream output,
                                  final Charset charset) throws IOException {
        if (lines == null) {
            return;
        }
        if (lineEnding == null) {
            lineEnding = System.lineSeparator();
        }
        final Charset cs = Charsets.toCharset(charset);
        for (final Object line : lines) {
            if (line != null) {
                output.write(line.toString().getBytes(cs));
            }
            output.write(lineEnding.getBytes(cs));
        }
    }


    public static void writeLines(final Collection<?> lines, final String lineEnding,
                                  final OutputStream output, final String charsetName) throws IOException {
        writeLines(lines, lineEnding, output, Charsets.toCharset(charsetName));
    }

 
    public static void writeLines(final Collection<?> lines, String lineEnding,
                                  final Writer writer) throws IOException {
        if (lines == null) {
            return;
        }
        if (lineEnding == null) {
            lineEnding = System.lineSeparator();
        }
        for (final Object line : lines) {
            if (line != null) {
                writer.write(line.toString());
            }
            writer.write(lineEnding);
        }
    }

 
    public static Writer writer(final Appendable appendable) {
        Objects.requireNonNull(appendable, "appendable");
        if (appendable instanceof Writer) {
            return (Writer) appendable;
        }
        if (appendable instanceof StringBuilder) {
            return new StringBuilderWriter((StringBuilder) appendable);
        }
        return new AppendableWriter<>(appendable);
    }


    public IOUtils() { 
    }

}
