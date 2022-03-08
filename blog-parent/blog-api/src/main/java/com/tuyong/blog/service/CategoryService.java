package com.tuyong.blog.service;

import com.tuyong.blog.vo.CategoryVo;
import com.tuyong.blog.vo.Result;

public interface CategoryService {
    CategoryVo findCategoryById(Long categoryId);

    Result findAll();

    Result findAllDetail();

    Result categoriesDetailById(Long id);
}
