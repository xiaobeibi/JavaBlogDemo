package com.tuyong.blog.service;

import com.tuyong.blog.vo.Result;
import com.tuyong.blog.vo.TagVo;

import java.util.List;

public interface TagsService {
    List<TagVo> findTagsByArticleId(Long id);

    List<TagVo> hot(int limit);

    Result findAll();

    Result findAllDetail();

    Result findDetailById(Long id);
}
