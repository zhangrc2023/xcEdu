package org.xuecheng.orders.model.dto;

import org.xuecheng.orders.model.po.XcPayRecord;
import lombok.Data;
import lombok.ToString;

/**
 * @version 1.0
 * @description 支付记录dto
 */
@Data
@ToString
public class PayRecordDto extends XcPayRecord {

    //支付二维码
    private String qrcode;

}
