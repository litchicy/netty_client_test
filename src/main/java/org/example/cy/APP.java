package org.example.cy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Hello world!
 */
public class APP {
    public static void main(String[] args) {
        MyClientHandler myClientHandler = new MyClientHandler();
        NioEventLoopGroup eventExecutors = new NioEventLoopGroup();
        try {
            //创建bootstrap对象，配置参数
            Bootstrap bootstrap = new Bootstrap();
            //设置线程组
            bootstrap.group(eventExecutors)
                    //设置客户端的通道实现类型
                    .channel(NioSocketChannel.class)
                    //使用匿名内部类初始化通道
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            //添加客户端通道的处理器
                            ch.pipeline().addLast(myClientHandler);
                        }
                    });
            System.out.println("客户端准备就绪，随时可以起飞~");
            //连接服务端
            ChannelFuture channelFuture = bootstrap.connect("8.130.42.107", 2181).sync();

            Channel channel = channelFuture.channel();
            Map<String, Object> map = new HashMap<>();
            map.put("username", "admin");
            map.put("password", "admin1");
            map.put("method", "login");
            map.put("user_ip", "127.0.0.1");

            String json = JSON.toJSONString(map, SerializerFeature.PrettyFormat, SerializerFeature.WriteNullListAsEmpty);

//            for(int i = 0; i < 500; i++) {
//                channel.writeAndFlush(Unpooled.copiedBuffer("dh236发给服务端循环信息中的第" + i + "条消息。", CharsetUtil.UTF_8));
                myClientHandler.sendMessage(Unpooled.copiedBuffer(json, CharsetUtil.UTF_8));
                myClientHandler.waitForResponse();
//            }

            //对通道关闭进行监听
            channel.closeFuture().sync();
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            //关闭线程组
            eventExecutors.shutdownGracefully();
        }
    }
}
