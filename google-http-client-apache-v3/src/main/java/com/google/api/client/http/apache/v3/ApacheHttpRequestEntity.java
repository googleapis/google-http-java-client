package com.google.api.client.http.apache.v3;


import org.apache.hc.core5.http.nio.AsyncEntityProducer;
import org.apache.hc.core5.http.nio.DataStreamChannel;
import org.apache.hc.core5.http.nio.entity.BasicAsyncEntityProducer;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.Set;

class ApacheHttpRequestEntityProducer implements AsyncEntityProducer {
    private ApacheHttpRequest request;

    private ApacheHttpRequestEntityProducer() {}

    public ApacheHttpRequestEntityProducer(ApacheHttpRequest request) {
        this.request = request;
    }

    @Override
    public boolean isRepeatable() {
        return false;
    }

    @Override
    public void failed(Exception e) {
        // noop
    }


    @Override
    public long getContentLength() {
        return request.getContentLength();
    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public String getContentEncoding() {
        return request.getContentEncoding();
    }

    @Override
    public boolean isChunked() {
        return false;
    }

    @Override
    public Set<String> getTrailerNames() {
        return Collections.emptySet();
    }

    /**
     * Borrowed from {@link BasicAsyncEntityProducer#available()}
     * @return
     */
    @Override
    public int available() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void produce(DataStreamChannel dataStreamChannel) throws IOException {
        if (request.getStreamingContent() == null) {
            return;
        }
        OutputStream dataStreamOutputStream = new OutputStream() {
            // diegomarquezp: the buffer size may have effects in performance - I just chose an arbitrary one
            private final java.nio.ByteBuffer buffer = ByteBuffer.allocate(100 * 1000);

            @Override
            public void write(int b) throws IOException {
                if (buffer.remaining() == 0) {
                    flush();
                }
                buffer.put((byte) b);
            }

            @Override
            public void flush() throws IOException {
                buffer.flip();
                dataStreamChannel.write(buffer);
                buffer.compact();
            }

            @Override
            public void close() throws IOException {
                flush();
                dataStreamChannel.endStream();
            }
        };
        request.getStreamingContent().writeTo(dataStreamOutputStream);
    }

    @Override
    public void releaseResources() {
        // no-op
    }
}
