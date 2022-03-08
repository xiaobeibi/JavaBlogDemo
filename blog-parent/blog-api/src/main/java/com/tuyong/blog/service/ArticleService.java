package com.tuyong.blog.service;

import com.tuyong.blog.dao.dos.Archives;
import com.tuyong.blog.vo.ArticleVo;
import com.tuyong.blog.vo.Result;
import com.tuyong.blog.vo.params.ArticleParam;
import com.tuyong.blog.vo.params.PageParams;

import java.util.List;

public interface ArticleService {
    List<ArticleVo> listArticlesPage(PageParams pageParams);

    List<ArticleVo> hotArticle(int limit);

    List<ArticleVo> newArticles(int limit);

    List<Archives> listArchives();

    ArticleVo findArticleById(Long id);

    Result publish(ArticleParam articleParam);
}
