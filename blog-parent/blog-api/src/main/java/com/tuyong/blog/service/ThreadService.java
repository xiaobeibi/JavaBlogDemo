package com.tuyong.blog.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.tuyong.blog.dao.mapper.ArticleMapper;
import com.tuyong.blog.dao.pojo.Article;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;

@Component
public class ThreadService {

    @Resource
    private ArticleMapper articleMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostConstruct
    public void initViewCount() {
        //为了 保证 启动项目的时候，redis中的浏览量 如果没有，读取数据库的数据，进行初始化
        //便于更新的时候 自增
        List<Article> articles = articleMapper.selectList(new LambdaQueryWrapper<>());
        for (Article article : articles) {
            String viewCountStr = (String) redisTemplate.opsForHash().get("view_count", String.valueOf(article.getId()));
            if (viewCountStr == null) {
                //初始化
                redisTemplate.opsForHash().put("view_count", String.valueOf(article.getId()), String.valueOf(article.getViewCounts()));
            }
        }
    }

    @Async("taskExecutor")
    public void updateViewCount(ArticleMapper articleMapper, Article article) {

//        Article articleUpdate = new Article();
//        articleUpdate.setViewCounts(article.getViewCounts() + 1);
//        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(Article::getId, article.getId());
//        queryWrapper.eq(Article::getViewCounts, article.getViewCounts());
//        articleMapper.update(articleUpdate, queryWrapper);
//        try {
//            //睡眠5秒 证明不会影响主线程的使用
//            Thread.sleep(5000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }

        redisTemplate.opsForHash().increment("view_count", String.valueOf(article.getId()), 1);
    }
}
