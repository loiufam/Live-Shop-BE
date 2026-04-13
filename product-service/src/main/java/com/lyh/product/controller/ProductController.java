package com.lyh.product.controller;

import com.lyh.common.result.Result;
import com.lyh.product.domain.Product;
import com.lyh.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/product")
@Slf4j
public class ProductController {

    @Autowired
    private ProductService productService;

    @RequestMapping("/selectByIdList")
    public Result<List<Product>> selectByIdList(@RequestParam("ids") List<Long> idList) {
        return Result.success(productService.selectByIdList(idList));
    }
}
