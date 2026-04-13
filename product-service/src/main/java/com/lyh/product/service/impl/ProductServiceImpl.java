package com.lyh.product.service.impl;

import com.lyh.product.domain.Product;
import com.lyh.product.mapper.ProductMapper;
import com.lyh.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Product> selectByIdList(List<Long> idList) {
        return productMapper.queryProductByIds(idList);
    }
}
