package org.example.xmf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Test
{
    public static void main(String[] args) throws InterruptedException
    {
        String id = "MA_TEST_1";
        String password = "MA_TEST_1";
        Map<String, Object> map = new HashMap<>();
        map.put("username", id);
        map.put("password", password);
        map.put("method", "login");
        map.put("model_type", "MagicDraw");
        String json = JSON.toJSONString(map, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteNullListAsEmpty);

        // 向服务器发送信息验证账号密码是否正确
        NettyClientHandler nettyClientHandler = new NettyClientHandler(json);
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>()
                {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception
                    {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                        pipeline.addLast(new IdleStateHandler(0, 1, 0, TimeUnit.SECONDS));
                        pipeline.addLast("handler", nettyClientHandler);
                    }
                });

        String serverIP = "127.0.0.1";
        // String serverIP = "8.130.42.107";
        int serverPort = 2181;
        ChannelFuture channelFuture = bootstrap.connect(serverIP, serverPort).sync();
        nettyClientHandler.sendMessage();
        nettyClientHandler.waitForResponse();

        // System.out.println(nettyClientHandler.getResponse());
        JSONObject jsonResponse = JSONObject.parseObject(nettyClientHandler.getResponse());
        int code = jsonResponse.getInteger("code");
        System.out.println("code: " + code);
        System.out.println("channel status-3: " + channelFuture.channel().isActive());
        channelFuture.channel().closeFuture().sync();
    }
}

