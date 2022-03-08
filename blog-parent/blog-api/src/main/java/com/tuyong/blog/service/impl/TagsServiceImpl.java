package com.tuyong.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tuyong.blog.dao.mapper.TagMapper;
import com.tuyong.blog.dao.pojo.Tag;
import com.tuyong.blog.service.TagsService;
import com.tuyong.blog.vo.Result;
import com.tuyong.blog.vo.TagVo;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class TagsServiceImpl implements TagsService {

    @Autowired
    private TagMapper tagMapper;

    @Override
    public List<TagVo> findTagsByArticleId(Long id) {
        List<Tag> tags = tagMapper.findTagsByArticleId(id);
        return copyList(tags);
    }

    @Override
    public List<TagVo> hot(int limit) {
        List<Long> hotsTagIds = tagMapper.findHotsTagIds(limit);
        if (CollectionUtils.isEmpty(hotsTagIds)) {
            return Collections.emptyList();
        }
        List<Tag> tagList = tagMapper.findTagsByTagIds(hotsTagIds);
        return copyList(tagList);
    }

    @Override
    public Result findAll() {
        LambdaQueryWrapper<Tag> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(Tag::getId, Tag::getTagName);
        List<Tag> tags = tagMapper.selectList(queryWrapper);
        return Result.success(copyList(tags));
    }

    @Override
    public Result findAllDetail() {
        List<Tag> tags = tagMapper.selectList(new LambdaQueryWrapper<>());
        return Result.success(copyList(tags));
    }

    @Override
    public Result findDetailById(Long id) {
        Tag tag = tagMapper.selectById(id);
        TagVo copy = copy(tag);
        return Result.success(copy);
    }

    private List<TagVo> copyList(List<Tag> tags) {
        List<TagVo> tagVoList = new ArrayList<>();
        for (Tag tag : tags) {
            tagVoList.add(copy(tag));
        }
        return tagVoList;
    }

    private TagVo copy(Tag tag) {
        TagVo tagVo = new TagVo();
        BeanUtils.copyProperties(tag, tagVo);
        tagVo.setId(String.valueOf(tag.getId()));
        return tagVo;
    }
}
