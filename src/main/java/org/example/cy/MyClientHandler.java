package org.example.cy;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class MyClientHandler extends ChannelInboundHandlerAdapter {

    private ChannelHandlerContext ctx;

//    控制等待回应的操作
    private CountDownLatch latch;

    public void sendMessage(Object message) {
//        发送消息的时候，将计数器的设置为1
        latch = new CountDownLatch(1);
        ctx.writeAndFlush(message);
    }

    private String response;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //发送消息到服务端
//        ctx.writeAndFlush(Unpooled.copiedBuffer("歪比巴卜~茉莉~Are you good~马来西亚~：dh自动发送的第一条消息。", CharsetUtil.UTF_8));
        this.ctx = ctx;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //接收服务端发送过来的消息
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println("收到服务端" + ctx.channel().remoteAddress() + "的消息：" + byteBuf.toString(CharsetUtil.UTF_8) + "\n");
        JSONObject jsonObject = JSONObject.parseObject(byteBuf.toString(CharsetUtil.UTF_8));
        this.response = byteBuf.toString(CharsetUtil.UTF_8);
        System.out.println(jsonObject.getString("code"));
        System.out.println(jsonObject.getString("message"));
        System.out.println(jsonObject.getString("data"));
//        收到服务器的消息的时候，将计数器减1
        latch.countDown();
    }

    public void waitForResponse() throws InterruptedException {
//        在等待回应的操作中，调用await方法进行等待，直到计数器为0，即可收到服务端的回应
        boolean success = latch.await(10, TimeUnit.SECONDS);
        if (!success) {
            System.out.println("丢失了服务器的返回的一条消息！！！ \n");
        }
    }

    public String getResponse() {
        return this.response;
    }
}
