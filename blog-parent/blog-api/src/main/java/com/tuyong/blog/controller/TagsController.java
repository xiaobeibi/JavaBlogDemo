package com.tuyong.blog.controller;

import com.tuyong.blog.service.TagsService;
import com.tuyong.blog.vo.Result;
import com.tuyong.blog.vo.TagVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("tags")
public class TagsController {

    @Autowired
    private TagsService tagsService;

    /**
     * 获取所有文章标签
     *
     * @return 文章标签
     */
    @GetMapping
    public Result findAll() {
        return tagsService.findAll();
    }

    /**
     * 查询所有文件标签详细信息
     *
     * @return 文章标签信息
     */
    @GetMapping("detail")
    public Result findAllDetail() {
        return tagsService.findAllDetail();
    }

    /**
     * 查询标签文章列表
     * @param id 文章ID
     * @return 标签文章
     */
    @GetMapping("detail/{id}")
    public Result findDetailById(@PathVariable("id") Long id) {
        return tagsService.findDetailById(id);
    }

    /**
     * 获取最热文章标签
     *
     * @return 最热文章标签
     */
    @GetMapping("/hot")
    public Result listHotTags() {
        int limit = 6;
        List<TagVo> tagVoList = tagsService.hot(limit);
        return Result.success(tagVoList);
    }
}
