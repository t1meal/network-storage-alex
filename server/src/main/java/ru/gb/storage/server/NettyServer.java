package ru.gb.storage.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.util.ArrayList;
import java.util.List;


public class NettyServer {
    private final int port;

    public static void main(String[] args) throws InterruptedException {
        new NettyServer(9000).start();
    }


    public NettyServer(int port) {
        this.port = port;
    }

    private void start() throws InterruptedException{
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap server = new ServerBootstrap();
            server
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
                            nioSocketChannel.pipeline().addLast(
                                    new ChannelInboundHandlerAdapter() {
                                        List <Object> allocator = new ArrayList<>();
                                        @Override
                                        public void channelRegistered(ChannelHandlerContext channelHandlerContext) throws Exception {
                                            System.out.println("channelRegistered!");
                                        }

                                        @Override
                                        public void channelUnregistered(ChannelHandlerContext channelHandlerContext) throws Exception {
                                            System.out.println("channelUnregistered!");
                                        }

                                        @Override
                                        public void channelActive(ChannelHandlerContext channelHandlerContext) throws Exception {
                                            System.out.println("channelActive!");
                                        }

                                        @Override
                                        public void channelInactive(ChannelHandlerContext channelHandlerContext) throws Exception {
                                            System.out.println("channelInactive!");
                                        }

                                        @Override
                                        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                                            Object no = null;
                                            if(msg == no){
                                                return;
                                            }
                                            ByteBuf in = (ByteBuf) msg;
                                            String str = in.toString(CharsetUtil.UTF_8);

                                            if (!str.contains("\n")) {
                                               String [] split = str.split("\n");
                                               if (split.length == 1){
                                                   ctx.writeAndFlush(split[0]);
                                               }
                                               for (int i = 0; i < split.length - 1; i++) {
                                                    ctx.writeAndFlush(split[i]);
                                               }
                                               if (split.length > 1){
                                                   allocator.add(split[split.length-1]);
                                               }

                                            } else {
                                                if(allocator.size() > 0 ) {
                                                    for (Object out : allocator) {
                                                        ctx.write(out);
                                                    }
                                                    ctx.flush();
                                                    System.out.println("Receive message to client!");
                                                    allocator.clear();
                                                    ReferenceCountUtil.release(msg);
                                                }
                                            }
                                        }

                                        @Override
                                        public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable) throws Exception {
                                            System.out.println("Caught exception!");
                                            throwable.printStackTrace();
                                            channelHandlerContext.close();
                                        }

                                    }
                            );
                        }
                    })
                    .option(ChannelOption.SO_BACKLOG, 128)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            Channel channel = server.bind(port).sync().channel();

            System.out.println("Server started!");
            channel.closeFuture().sync();

        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
