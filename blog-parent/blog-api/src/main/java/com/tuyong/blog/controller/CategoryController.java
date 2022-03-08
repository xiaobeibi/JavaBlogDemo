package com.tuyong.blog.controller;

import com.tuyong.blog.service.CategoryService;
import com.tuyong.blog.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("categorys")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public Result listCategory() {
        return categoryService.findAll();
    }

    /**
     *  查询所有的文章分类
     * @return 文章分类结果
     */
    @GetMapping("detail")
    public Result categoriesDetail(){
        return categoryService.findAllDetail();
    }

    /**
     *  查询分类文章列表
     * @param id 文章ID
     * @return 分类文章列表
     */
    @GetMapping("detail/{id}")
    public Result categoriesDetailById(@PathVariable("id") Long id){
        return categoryService.categoriesDetailById(id);
    }
}
