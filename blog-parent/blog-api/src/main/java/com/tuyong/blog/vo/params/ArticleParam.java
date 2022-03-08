package com.tuyong.blog.vo.params;

import com.tuyong.blog.vo.CategoryVo;
import com.tuyong.blog.vo.TagVo;
import lombok.Data;

import java.util.List;

@Data
public class ArticleParam {

    private Long id;

    private ArticleBodyParam body;

    private CategoryVo category;

    private String summary;

    private List<TagVo> tags;

    private String title;
}
