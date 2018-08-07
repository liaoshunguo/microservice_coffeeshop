package com.lijingyao.microservice.coffee.trade.service;

import com.lijingyao.microservice.coffee.base.rest.BaseService;
import com.lijingyao.microservice.coffee.base.rest.CommonErrors;
import com.lijingyao.microservice.coffee.base.rest.ServiceResult;
import com.lijingyao.microservice.coffee.template.trade.OrderCreateDTO;
import com.lijingyao.microservice.coffee.template.trade.OrderDTO;
import com.lijingyao.microservice.coffee.template.trade.OrderDetailDTO;
import com.lijingyao.microservice.coffee.trade.errors.TradeError;
import com.lijingyao.microservice.coffee.trade.persistence.entity.TradeOrder;
import com.lijingyao.microservice.coffee.trade.persistence.entity.TradeOrderDetail;
import com.lijingyao.microservice.coffee.trade.persistence.repository.OrderDetailRepository;
import com.lijingyao.microservice.coffee.trade.persistence.repository.OrderRepository;
import com.lijingyao.microservice.coffee.trade.restapi.assemblers.OrderAssembler;
import com.lijingyao.microservice.coffee.trade.service.validators.OrderValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created by lijingyao on 2018/7/8 23:33.
 */
@Service
public class OrderService extends BaseService {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private OrderAssembler orderAssembler;
    @Autowired
    private OrderValidator orderValidator;

    @Transactional
    public ServiceResult<OrderDTO> createOrder(OrderCreateDTO createDTO) {
        ServiceResult<OrderDTO> result = getResult();

        if (orderValidator.validateOrderCreateDTO().negate().test(createDTO)) {
            return result.setErrors(CommonErrors.ILLEGAL_PARAM_ERROR);
        }

        TradeOrder order = orderAssembler.assembleOrder(createDTO);

        Optional<List<TradeOrderDetail>> orderDetailOpt = orderAssembler.assembleDetailOrders(createDTO, order);

        if (!orderDetailOpt.isPresent()) {
            return result.setErrors(TradeError.ORDER_DETAIL_NOT_EXIST);
        }

        List<TradeOrderDetail> details = orderDetailOpt.get();
        long totalPrice = details.stream().filter(o -> o.getPrice() != null && o.getPrice() > 0).mapToLong(o -> o.getPrice()).sum();

        order.setPaymentPrice(totalPrice);

        orderRepository.save(order);
        orderDetailRepository.save(details);

        OrderDTO orderDTO = orderAssembler.assembleOrderDTO(order, details);
        return result.setResult(orderDTO);
    }


    public ServiceResult<List<OrderDTO>> getUserNewOrders(Long userId, Integer newOrderSize) {
        ServiceResult<List<OrderDTO>> result = getResult();
        Pageable pageable = new PageRequest(0, newOrderSize, new Sort(Sort.Direction.DESC, "utcCreate"));
        List<TradeOrder> orders = orderRepository.findByUserId(userId, pageable);

        if (CollectionUtils.isEmpty(orders)) {
            return result.setResult(Collections.EMPTY_LIST);
        }

        List<OrderDTO> orderDTOs = orders.stream().map(o -> orderAssembler.assembleOrderDTO(o,null)).collect(Collectors.toList());

        return result.setResult(orderDTOs);
    }

    public ServiceResult<List<OrderDetailDTO>> getDetailOrders(ArrayList<String> mainOrderIds,Integer orderSize) {
        ServiceResult<List<OrderDetailDTO>> result = getResult();
        Pageable detailPageable = new PageRequest(0, orderSize, new Sort(Sort.Direction.DESC, "utcCreate"));
        List<TradeOrderDetail> details = orderDetailRepository.findByMainOrderIds(mainOrderIds, detailPageable);


        if (CollectionUtils.isEmpty(details)) {
            return result.setResult(Collections.EMPTY_LIST);
        }

        List<OrderDetailDTO> orderDTOs = details.stream().map(o -> orderAssembler.assembleOrderDetailDTO(o)).collect(Collectors.toList());

        return result.setResult(orderDTOs);
    }
}
