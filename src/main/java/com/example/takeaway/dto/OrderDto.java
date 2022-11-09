package com.example.takeaway.dto;

import com.example.takeaway.entity.OrderDetail;
import com.example.takeaway.entity.Orders;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class OrderDto extends Orders {

    private List<OrderDetail> orderDetails;

    private String categoryName;

    private LocalDateTime beginTime;

    private LocalDateTime endTime;
}
