package org.xuecheng.orders.service;


import org.xuecheng.messagesdk.model.po.MqMessage;
import org.xuecheng.orders.model.dto.AddOrderDto;
import org.xuecheng.orders.model.dto.PayRecordDto;
import org.xuecheng.orders.model.dto.PayStatusDto;
import org.xuecheng.orders.model.po.XcPayRecord;

/**
 * @version 1.0
 * @description 订单相关的service接口
 */
public interface OrderService {

    /**
     * @param addOrderDto 订单信息
     * @return PayRecordDto 支付记录(包括二维码)
     * @description 创建商品订单
     */
    public PayRecordDto createOrder(String userId, AddOrderDto addOrderDto);

    /**
     * @param payNo 交易记录号
     * @return com.xuecheng.orders.model.po.XcPayRecord
     * @description 查询支付记录
     */
//    public XcPayRecord getPayRecordByPayno(String payNo);

    /**
     * 请求支付宝查询支付结果
     * @param payNo 支付记录id
     * @return 支付记录信息
     */
    public PayRecordDto queryPayResult(String payNo);

    /**
     * 保存支付状态
     * @param payStatusDto
     */
    public void saveAliPayStatus(PayStatusDto payStatusDto);

    /**
     * 发送通知结果（消息队列RabbitMQ）
     * @param message
     */
    public void notifyPayResult(MqMessage message);
}
