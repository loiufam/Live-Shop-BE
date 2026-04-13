package com.lyh.product.service;

import com.lyh.product.domain.Product;

import java.util.List;

public interface ProductService {
    List<Product> selectByIdList(List<Long> idList);
}
