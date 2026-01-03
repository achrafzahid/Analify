package com.analyfy.analify.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import com.analyfy.analify.DTO.OrderDTO;
import com.analyfy.analify.Entity.Order;

@Mapper(componentModel = "spring", uses = {OrderItemsMapper.class})
public interface OrderMapper extends BaseMapper<OrderDTO, Order> {

    @Override
    // 1. Map Cashier Details
    @Mapping(source = "caissier.userId", target = "cashierId")
    @Mapping(source = "caissier.userName", target = "cashierName")

    // 2. Map Store Details (Through the Caissier!)
    // Since Order doesn't have 'store', we go order.getCaissier().getStore()
    @Mapping(source = "caissier.store.storeId", target = "storeId")
    @Mapping(source = "caissier.store.city.name", target = "storeName") // Assuming you want City Name as Store Name

    // 3. Calculated Fields
    @Mapping(target = "totalItems", expression = "java(entity.getItems() != null ? entity.getItems().size() : 0)")
    @Mapping(target = "totalAmount", expression = "java(calculateTotal(entity))")
    OrderDTO toDto(Order entity);

    // Calculation Logic (Looks perfect)
    default Double calculateTotal(Order order) {
        if (order.getItems() == null) return 0.0;
        return order.getItems().stream()
            .mapToDouble(i -> (i.getPrice() - (i.getDiscount() != null ? i.getDiscount() : 0.0)) * i.getQuantity())
            .sum();
    }
}