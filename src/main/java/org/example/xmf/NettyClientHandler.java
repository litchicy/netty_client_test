package org.example.xmf;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class NettyClientHandler extends ChannelInboundHandlerAdapter
{
    private final String json;

    // 控制等待回应的操作
    private CountDownLatch latch;
    private String response;

    // 客户端请求的心跳命令
    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("hb_request", CharsetUtil.UTF_8));
    private int fcount = 1;  // 心跳循环次数

    public NettyClientHandler(String json)
    {
        this.json = json;
    }

    public void sendMessage()
    {
        latch = new CountDownLatch(1);  // 发送消息的时候，将计数器的设置为1
        // ctx.writeAndFlush(message);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception
    {
        System.out.println("channel status-1: " + ctx.channel().isActive());
        ctx.writeAndFlush(Unpooled.copiedBuffer(json, CharsetUtil.UTF_8));
        ctx.fireChannelActive();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception
    {
        // 接收服务端发送过来的消息
        ByteBuf byteBuf = (ByteBuf) msg;
        // System.out.println("收到服务端" + ctx.channel().remoteAddress() + "的消息：" + byteBuf.toString(CharsetUtil.UTF_8) + "\n");
        this.response = byteBuf.toString(CharsetUtil.UTF_8);
        System.out.println(byteBuf.toString(CharsetUtil.UTF_8));
        JSONObject jsonObject = JSONObject.parseObject(byteBuf.toString(CharsetUtil.UTF_8));
        System.out.println(date());
        Thread.sleep(10000);
        System.out.println(date());
        String code = jsonObject.getString("code");
        if(!code.equals("200")) {
            Map<String, String> map = updateModelData(byteBuf);
            String updateRes = JSON.toJSONString(map, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                    SerializerFeature.WriteDateUseDateFormat, SerializerFeature.WriteNullListAsEmpty);
            ctx.writeAndFlush(Unpooled.copiedBuffer(updateRes, CharsetUtil.UTF_8));
        }
        // 收到服务器的消息的时候，将计数器减1
        latch.countDown();
        System.out.println("channel status-2: " + ctx.channel().isActive());
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception
    {
        System.out.println("[INFO] time: " + date() + ", count: " + fcount);
        if (obj instanceof IdleStateEvent)
        {
            IdleStateEvent event = (IdleStateEvent) obj;
            if (IdleState.WRITER_IDLE.equals(event.state()))
            {
                ctx.channel().writeAndFlush(HEARTBEAT_SEQUENCE.duplicate());
            }
        }
        fcount++;
    }

    public void waitForResponse() throws InterruptedException
    {
        // 在等待回应的操作中，调用await方法进行等待，直到计数器为0，即可收到服务端的回应
        boolean success = latch.await(20, TimeUnit.SECONDS);
        if (!success)
        {
            System.out.println("丢失了服务器的返回的一条消息！！！ \n");
        }
    }

    public String getResponse()
    {
        return this.response;
    }

    private String date()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(new Date());
    }

    private Map<String, String> updateModelData(ByteBuf byteBuf) {
        JSONObject jsonObject = JSONObject.parseObject(byteBuf.toString(CharsetUtil.UTF_8));
        String code = jsonObject.getString("code");
        Map<String, String> resMap = new HashMap<>();
        resMap.put("method", "responseModifyModelData");
        if(code.equals("4100")) {
            resMap.put("response", "success");
            return resMap;
        }
        resMap.put("response", "error");
        return resMap;
    }
}
