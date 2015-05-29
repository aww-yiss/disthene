package net.iponweb.disthene.carbon;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;
import net.iponweb.disthene.bean.Metric;
import net.iponweb.disthene.bus.DistheneBus;
import net.iponweb.disthene.config.Rollup;
import net.iponweb.disthene.events.MetricReceivedEvent;
import org.apache.log4j.Logger;

/**
 * @author Andrei Ivanov
 */
public class CarbonServerHandler extends ChannelInboundHandlerAdapter {
    private Logger logger = Logger.getLogger(CarbonServerHandler.class);

    private DistheneBus bus;
    private Rollup rollup;

    public CarbonServerHandler(DistheneBus bus, Rollup rollup) {
        this.bus = bus;
        this.rollup = rollup;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf in = (ByteBuf) msg;
        Metric metric = new Metric(in.toString(CharsetUtil.UTF_8).trim(), rollup);
        in.release();

        bus.post(new MetricReceivedEvent(metric));
    }
}
