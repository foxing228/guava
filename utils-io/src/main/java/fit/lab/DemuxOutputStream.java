package main.java.fit.lab;

import java.io.IOException;
import java.io.OutputStream;

public class DemuxOutputStream
    extends OutputStream
{
    private final InheritableThreadLocal<OutputStream> m_streams = new InheritableThreadLocal<OutputStream>();

        public OutputStream bindStream( OutputStream output )
    {
        OutputStream stream = m_streams.get();
        m_streams.set( output );
        return stream;
    }

        @Override
    public void close()
        throws IOException
    {
        OutputStream output = m_streams.get();
        if( null != output )
        {
            output.close();
        }
    }

        @Override
    public void flush()
        throws IOException
    {
        OutputStream output = m_streams.get();
        if( null != output )
        {
            output.flush();
        }
    }

        @Override
    public void write( int ch )
        throws IOException
    {
        OutputStream output = m_streams.get();
        if( null != output )
        {
            output.write( ch );
        }
    }
}
