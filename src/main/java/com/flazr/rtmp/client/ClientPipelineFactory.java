/*
 * Flazr <http://flazr.com> Copyright (C) 2009  Peter Thomas.
 *
 * This file is part of Flazr.
 *
 * Flazr is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Flazr is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Flazr.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.flazr.rtmp.client;

import com.flazr.rtmp.*;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientPipelineFactory implements ChannelPipelineFactory {
    
    private static final Logger logger = LoggerFactory.getLogger(ClientPipelineFactory.class);  

    private final ClientOptions options;

    public ClientPipelineFactory(final ClientOptions options) {
        this.options = options;
    }

    @Override
    public ChannelPipeline getPipeline() {
        final ChannelPipeline pipeline = Channels.pipeline();
        ProtocolType protocol = options.getProtocol();
        if (protocol == ProtocolType.RTMPS) {
            logger.info("{} requested, initializing SSL", protocol);
//        SSLEngine engine = DummySslContextFactory.getClientContext().createSSLEngine();
//        engine.setUseClientMode(true);
//        pipeline.addLast("ssl", new SslHandler(engine));
        }
        if (protocol == ProtocolType.RTMPS || protocol == ProtocolType.RTMPT) {
            logger.info("{} requested, initializing http tunnel", protocol);
            pipeline.addLast("httpcodec", new HttpClientCodec());
            pipeline.addLast("httpchunk", new HttpChunkAggregator(1048576));
            pipeline.addLast("httptunnel", new ClientHttpTunnelHandler(options));             
        }
        pipeline.addLast("handshaker", new ClientHandshakeHandler(options));
        pipeline.addLast("decoder", new RtmpDecoder());
        pipeline.addLast("encoder", new RtmpEncoder());
        pipeline.addLast("handler", new ClientHandler(options));
        return pipeline;
    }

}
