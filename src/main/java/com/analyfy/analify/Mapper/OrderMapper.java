package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.analyfy.analify.DTO.OrderDTO;
import com.analyfy.analify.Entity.Order;

@Mapper(componentModel = "spring", uses = {OrderItemsMapper.class})
public interface OrderMapper extends BaseMapper<OrderDTO, Order> {
    @Override
    @Mapping(source = "caissier.userId", target = "cashierId")
    @Mapping(source = "caissier.userName", target = "cashierName")
    @Mapping(source = "store.storeId", target = "storeId")
    @Mapping(source = "store.city.name", target = "storeName")
    @Mapping(target = "totalItems", expression = "java(entity.getItems() != null ? entity.getItems().size() : 0)")
    @Mapping(target = "totalAmount", expression = "java(calculateTotal(entity))")
    OrderDTO toDto(Order entity);

    default Double calculateTotal(Order order) {
        if (order.getItems() == null) return 0.0;
        return order.getItems().stream()
            .mapToDouble(i -> (i.getPrice() - (i.getDiscount() != null ? i.getDiscount() : 0)) * i.getQuantity())
            .sum();
    }
}