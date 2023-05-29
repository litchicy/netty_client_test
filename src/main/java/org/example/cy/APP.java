package org.example.cy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.JSONObject;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;
import org.example.Producer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Hello world!
 */
public class APP {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool(); // 创建一个根据需求自动扩容的线程池
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
            map.put("username", "MA_TEST_2");
            map.put("password", "MA_TEST_2");
            map.put("method", "login");
            map.put("model_type", "MagicDraw");

            String json = JSON.toJSONString(map, SerializerFeature.PrettyFormat, SerializerFeature.WriteNullListAsEmpty);

//            for(int i = 0; i < 500; i++) {
//                channel.writeAndFlush(Unpooled.copiedBuffer("dh236发给服务端循环信息中的第" + i + "条消息。", CharsetUtil.UTF_8));
                myClientHandler.sendMessage(Unpooled.copiedBuffer(json, CharsetUtil.UTF_8));
                myClientHandler.waitForResponse();
            System.out.println("=====");
//            produceFile();
//            System.out.println(myClientHandler.getResponse());
//            }
            System.out.println(myClientHandler.getResponse());
            executorService.execute(() -> {
                try {
//                    对关闭通道进行监听，获取Channel的CloseFuture，并且阻塞当前线程直到它完成
                    channel.closeFuture().sync();
                }
                catch(InterruptedException e) {
                    e.printStackTrace();
                }
                finally {
                    eventExecutors.shutdownGracefully();

                }
            });
        } catch(InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void produceFile()
    {
        Map<String, Object> map = new HashMap<>();
        map.put("user_id", "Ucb23c8929f3c4d549d3968db83c0953f");
        map.put("user_ip", "127.0.0.1");
        map.put("model_type", "MagicDraw");
        map.put("model_id", "");
        map.put("project_id", "P19442cac16cb4584aaffd4097ff45b36");
        map.put("file_name", "create_test.json");
        map.put("cover", "true");
        map.put("date", "2023-5-19 17:31");
        String json = JSON.toJSONString(map, SerializerFeature.PrettyFormat, SerializerFeature.WriteNullListAsEmpty);
        JSONObject jsonObject = JSONObject.parseObject(json);

        String QUEUE_NAME = "test";
        String host = "8.130.42.107";
        String virtualHost = "/";
        String userName = "admin";
        String userPassword = "123456";
        int port = 5672;
        Producer producer = new Producer(QUEUE_NAME, jsonObject, host, port, virtualHost, userName, userPassword);
        boolean flag = producer.send();
        System.out.println(flag);
    }
}
