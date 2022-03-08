package com.tuyong.blog.controller;

import com.tuyong.blog.service.CommentsService;
import com.tuyong.blog.vo.Result;
import com.tuyong.blog.vo.params.CommentParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("comments")
public class CommentsController {

    @Autowired
    private CommentsService commentsService;

    /**
     * 评论列表
     * @param articleId 文章ID
     * @return 评论视图
     */
    @GetMapping("article/{id}")
    public Result comments(@PathVariable("id") Long articleId) {
        return commentsService.commentsByArticleId(articleId);
    }

    /**
     * 添加评论功能
     * @param commentParam 评论参数
     * @return 添加评论结果
     */
    @PostMapping("create/change")
    public Result comment(@RequestBody CommentParam commentParam){
        return commentsService.comment(commentParam);
    }
}
