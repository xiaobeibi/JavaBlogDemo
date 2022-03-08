package com.tuyong.blog.service;

import com.tuyong.blog.vo.Result;
import com.tuyong.blog.vo.params.CommentParam;

public interface CommentsService {

    Result commentsByArticleId(Long articleId);

    Result comment(CommentParam commentParam);
}
