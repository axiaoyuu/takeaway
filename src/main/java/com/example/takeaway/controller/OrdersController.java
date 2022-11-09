package com.example.takeaway.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.takeaway.common.BaseContext;
import com.example.takeaway.common.R;
import com.example.takeaway.dto.OrderDto;
import com.example.takeaway.entity.OrderDetail;
import com.example.takeaway.entity.Orders;
import com.example.takeaway.entity.ShoppingCart;
import com.example.takeaway.service.OrderDetailService;
import com.example.takeaway.service.OrdersService;
import com.example.takeaway.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/order")
public class OrdersController {

    @Autowired
    private OrdersService orderService;

    @Autowired
    private OrdersService ordersService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 订单的分页查询
     *
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String number, String beginTime, String endTime) {
        //构造分页构造器
        Page<Orders> ordersPageInfo = new Page<>(page, pageSize);
        Page<OrderDto> ordersDtoPageInfo = new Page<>();

        //构造条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //根据 number 进行模糊查询
        queryWrapper.like(!StringUtils.isEmpty(number), Orders::getNumber, number);
        //根据 Datetime 进行时间范围查询
        if (beginTime != null && endTime != null) {
            queryWrapper.ge(Orders::getOrderTime, beginTime);//ge ：大于等于 >=
            queryWrapper.le(Orders::getOrderTime, endTime);//le ：小于等于 <=
        }

        //添加排序条件
        queryWrapper.orderByDesc(Orders::getOrderTime);

        //进行分页查询
        orderService.page(ordersPageInfo, queryWrapper);

        //对象拷贝
        BeanUtils.copyProperties(ordersPageInfo, ordersDtoPageInfo, "records");

        List<Orders> records = ordersPageInfo.getRecords();

        List<OrderDto> list = records.stream().map((item) -> {
            OrderDto ordersDto = new OrderDto();
            BeanUtils.copyProperties(item, ordersDto);

            //此处相当于是给 userName 赋值 consignee_userId
            String consignee = item.getConsignee();
            String name = consignee + "_" + item.getUserId();
            ordersDto.setUserName(name);

            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPageInfo.setRecords(list);
        return R.success(ordersDtoPageInfo);
    }

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Orders orders){
        orderService.submit(orders);
        return R.success("下单成功");
    }

    /**
     * 用户端展示自己的订单分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    public R<Page> page(int page,int pageSize){
        //分页构造器对象
        Page<Orders> pageInfo = new Page<>(page, pageSize);
        Page<OrderDto> pageDto = new Page<>(page, pageSize);
        //构造条件查询对象
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId,BaseContext.getCurrentId());
        //显示当前用户分页的信息，需要添加用户id作为查询，防止用户能查询别人订单
        //以时间为排序
        queryWrapper.orderByDesc(Orders::getOrderTime);
        orderService.page(pageInfo,queryWrapper);

        //沟通OrderId查询对应的OrderDetail
        //LambdaQueryWrapper<OrderDetail> orderDetailLambdaQueryWrapper = new LambdaQueryWrapper<>();

        //对OrderDto进行属性赋值
        List<Orders> records = pageInfo.getRecords();
        List<OrderDto> ordersDtoList = records.stream().map((item)->{
            OrderDto orderDto = new OrderDto();
            //获取订单ID
            Long orderId = item.getId();
            List<OrderDetail> orderDetailList = ordersService.getOrderDetailListByOrderId(orderId);
            BeanUtils.copyProperties(item,orderDto);
            //对orderDto进行OrderDetails属性的赋值
            orderDto.setOrderDetails(orderDetailList);
            return orderDto;
        }).collect(Collectors.toList());

        //获取分页信息
        BeanUtils.copyProperties(pageInfo,pageDto,"records");
        pageDto.setRecords(ordersDtoList);
        return R.success(pageDto);
    }

    /**
     * 订单派送
     * @param map
     * @return
     */
    @PutMapping
    public R<String> orderStatusChange(@RequestBody Map<String,String> map){
        String id = map.get("id");
        Long orderId = Long.parseLong(id);
        Integer status = Integer.parseInt(map.get("status"));

        if (orderId == null || status == null ){
            return R.error("传入信息不合法");
        }
        Orders orders = orderService.getById(orderId);
        orders.setStatus(status);
        orderService.updateById(orders);

        return R.success("订单修改成功");
    }

    //客户端点击再来一单
    /**
     * 前端点击再来一单是直接跳转到购物车的，所以为了避免数据有问题，再跳转之前我们需要把购物车的数据给清除
     * ①通过orderId获取订单明细
     * ②把订单明细的数据的数据塞到购物车表中，不过在此之前要先把购物车表中的数据给清除(清除的是当前登录用户的购物车表中的数据)，
     * 不然就会导致再来一单的数据有问题；
     * (这样可能会影响用户体验，但是对于外卖来说，用户体验的影响不是很大，电商项目就不能这么干了)
     */
    @PostMapping("/again")
    public R<String> againSubmit(@RequestBody Map<String,String> map){
        String ids = map.get("id");

        long id = Long.parseLong(ids);

        LambdaQueryWrapper<OrderDetail> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderDetail::getOrderId,id);
        //获取该订单对应的所有的订单明细表
        List<OrderDetail> orderDetailList = orderDetailService.list(queryWrapper);

        //通过用户id把原来的购物车给清空，这里的clean方法是视频中讲过的,建议抽取到service中,那么这里就可以直接调用了
        LambdaQueryWrapper<ShoppingCart> shoppingCartLambdaQuery = new LambdaQueryWrapper<>();
        shoppingCartLambdaQuery.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        shoppingCartService.remove(shoppingCartLambdaQuery);

        //获取用户id
        Long userId = BaseContext.getCurrentId();
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map((item) -> {
            //把从order表中和order_details表中获取到的数据赋值给这个购物车对象
            ShoppingCart shoppingCart = new ShoppingCart();
            shoppingCart.setUserId(userId);
            shoppingCart.setImage(item.getImage());
            Long dishId = item.getDishId();
            Long setmealId = item.getSetmealId();
            if (dishId != null) {
                //如果是菜品那就添加菜品的查询条件
                shoppingCart.setDishId(dishId);
            } else {
                //添加到购物车的是套餐
                shoppingCart.setSetmealId(setmealId);
            }
            shoppingCart.setName(item.getName());
            shoppingCart.setDishFlavor(item.getDishFlavor());
            shoppingCart.setNumber(item.getNumber());
            shoppingCart.setAmount(item.getAmount());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        //把携带数据的购物车批量插入购物车表  这个批量保存的方法要使用熟练！！！
        shoppingCartService.saveBatch(shoppingCartList);

        return R.success("操作成功");
    }
}
