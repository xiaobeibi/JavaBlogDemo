package com.tuyong.blog.controller;

import com.tuyong.blog.common.cache.Cache;
import com.tuyong.blog.service.ArticleService;
import com.tuyong.blog.vo.ArticleVo;
import com.tuyong.blog.vo.Result;
import com.tuyong.blog.vo.params.ArticleParam;
import com.tuyong.blog.vo.params.PageParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("articles")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    /**
     * 文章列表
     *
     * @param pageParams 文章页面
     * @return 统一返回文章列表
     */
    @PostMapping
//    @Cache(expire = 5 * 60 * 1000, name = "listArticle")
    public Result articles(@RequestBody PageParams pageParams) {

        return Result.success(articleService.listArticlesPage(pageParams));
    }

    /**
     * 最热文章
     *
     * @return 统一返回文章列表
     */
    @PostMapping("hot")
//    @Cache(expire = 5 * 60 * 1000, name = "view_article")
    public Result hotArticle() {
        int limit = 5;
        return Result.success(articleService.hotArticle(limit));
    }

    /**
     * 最新文章
     *
     * @return 统一返回文章列表
     */
    @PostMapping("new")
//    @Cache(expire = 5 * 60 * 1000, name = "view_article")
    public Result newArticles() {
        int limit = 5;
        return Result.success(articleService.newArticles(limit));
    }

    /**
     * 文章归档
     *
     * @return 统一返回文章列表
     */
    @PostMapping("listArchives")
    public Result listArchives() {
        return Result.success(articleService.listArchives());
    }

    /**
     * 根据文章ID查看文章详情
     *
     * @param id 文章id
     * @return 统一返回
     */
    @PostMapping("view/{id}")
    public Result findArticleById(@PathVariable("id") Long id) {
        return Result.success(articleService.findArticleById(id));
    }

    /**
     * 发布文章
     *
     * @param articleParam 文章参数
     * @return 文章ID
     */
    @PostMapping("publish")
    public Result publish(@RequestBody ArticleParam articleParam) {
        return articleService.publish(articleParam);
    }



    @PostMapping("{id}")
    public Result articleById(@PathVariable("id") Long articleId) {
        return Result.success(articleService.findArticleById(articleId));
    }
}
