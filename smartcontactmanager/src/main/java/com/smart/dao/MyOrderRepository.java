package com.smart.dao;

import org.springframework.data.repository.CrudRepository;

import com.smart.entities.MyOrder;

public interface MyOrderRepository extends CrudRepository<MyOrder, Long> {
	
	public MyOrder findByOrderId(String id);
}
