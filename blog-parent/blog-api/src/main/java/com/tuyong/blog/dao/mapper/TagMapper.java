package com.tuyong.blog.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.tuyong.blog.dao.pojo.Tag;

import java.util.List;

public interface TagMapper extends BaseMapper<Tag> {
    List<Tag> findTagsByArticleId(Long articleId);

    List<Long> findHotsTagIds(int limit);

    List<Tag> findTagsByTagIds(List<Long> tagIds);
}
