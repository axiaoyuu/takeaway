package com.example.takeaway.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.takeaway.entity.OrderDetail;
import com.example.takeaway.entity.Orders;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

public interface OrdersService extends IService<Orders> {

    /**
     * 用户下单
     * @param orders
     */
    public void submit(Orders orders);

    public List<OrderDetail> getOrderDetailListByOrderId(Long orderId);
}
