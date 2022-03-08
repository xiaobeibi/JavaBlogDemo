package com.tuyong.blog.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.tuyong.blog.dao.dos.Archives;
import com.tuyong.blog.dao.mapper.ArticleBodyMapper;
import com.tuyong.blog.dao.mapper.ArticleMapper;
import com.tuyong.blog.dao.mapper.ArticleTagMapper;
import com.tuyong.blog.dao.pojo.Article;
import com.tuyong.blog.dao.pojo.ArticleBody;
import com.tuyong.blog.dao.pojo.ArticleTag;
import com.tuyong.blog.dao.pojo.SysUser;
import com.tuyong.blog.service.*;
import com.tuyong.blog.utils.UserThreadLocal;
import com.tuyong.blog.vo.*;
import com.tuyong.blog.vo.params.ArticleParam;
import com.tuyong.blog.vo.params.PageParams;
import org.joda.time.DateTime;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleMapper articleMapper;
    @Autowired
    private SysUserService sysUserService;
    @Autowired
    private TagsService tagsService;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private ArticleBodyMapper articleBodyMapper;
    @Autowired
    private ThreadService threadService;
    @Autowired
    private ArticleTagMapper articleTagMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;

/*
    @Override
    public List<ArticleVo> listArticlesPage(PageParams pageParams) {
        Page<Article> page = new Page<>(pageParams.getPage(), pageParams.getPageSize());
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        if (pageParams.getCategoryId() != null) {
            queryWrapper.eq(Article::getCategoryId, pageParams.getCategoryId());
        }

        List<Long> articleIdList = new ArrayList<>();
        if (pageParams.getTagId() != null) {
            LambdaQueryWrapper<ArticleTag> articleTagLambdaQueryWrapper = new LambdaQueryWrapper<>();
            articleTagLambdaQueryWrapper.eq(ArticleTag::getTagId, pageParams.getTagId());
            List<ArticleTag> articleTags = articleTagMapper.selectList(articleTagLambdaQueryWrapper);
            for (ArticleTag articleTag : articleTags) {
                articleIdList.add(articleTag.getArticleId());
            }
            if (articleIdList.size() > 0) {
                queryWrapper.in(Article::getId, articleIdList);
            }
        }

        // 置顶排序 + 时间排序
        queryWrapper.orderByDesc(Article::getWeight, Article::getCreateDate);
        Page<Article> articlePage = articleMapper.selectPage(page, queryWrapper);
        List<Article> articles = articlePage.getRecords();
        return copyList(articles, true, true);
    }
*/

    @Override
    public List<ArticleVo> listArticlesPage(PageParams pageParams) {
        Page<Article> page = new Page<>(pageParams.getPage(), pageParams.getPageSize());
        IPage<Article> articleIPage = articleMapper.listArticle(page, pageParams.getCategoryId(), pageParams.getTagId(), pageParams.getYear(), pageParams.getMonth());
        List<Article> records = articleIPage.getRecords();
        for (Article record : records) {
            String viewCount = (String) redisTemplate.opsForHash().get("view_count", String.valueOf(record.getId()));
            if (viewCount != null) {
                record.setViewCounts(Integer.parseInt(viewCount));
            }
        }
        return copyList(records, true, true);
    }

    @Override
    public List<ArticleVo> hotArticle(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getViewCounts);
        queryWrapper.select(Article::getId, Article::getTitle);
        queryWrapper.last("limit " + limit);
        // select id,title from article order by view_counts desc limit 5
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return copyList(articles, false, false);
    }

    @Override
    public List<ArticleVo> newArticles(int limit) {
        LambdaQueryWrapper<Article> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Article::getCreateDate);
        queryWrapper.select(Article::getId, Article::getTitle);
        queryWrapper.last("limit " + limit);
        // select id,title from article order by create_date desc limit 5
        List<Article> articles = articleMapper.selectList(queryWrapper);
        return copyList(articles, false, false);
    }

    @Override
    public List<Archives> listArchives() {
        return articleMapper.listArchives();
    }

    @Override
    public ArticleVo findArticleById(Long id) {
        Article article = articleMapper.selectById(id);
        ArticleVo articleVo = copy(article, true, true,true,true);
        threadService.updateViewCount(articleMapper, article);
        String viewCount = (String) redisTemplate.opsForHash().get("view_count", String.valueOf(id));
        if (viewCount != null){
            articleVo.setViewCounts(Integer.parseInt(viewCount));
        }
        return articleVo;
    }

    @Override
    @Transactional
    public Result publish(ArticleParam articleParam) {
        SysUser sysUser = UserThreadLocal.get();
        boolean isEdit = false;
        Article article = new Article();
        if (articleParam.getId() != null) {
            article = new Article();
            article.setId(articleParam.getId());
            article.setTitle(articleParam.getTitle());
            article.setSummary(articleParam.getSummary());
            article.setCategoryId(Long.parseLong(articleParam.getCategory().getId()));
            articleMapper.updateById(article);
            isEdit = true;
        } else {
            article.setAuthorId(sysUser.getId());
            article.setCategoryId(Long.parseLong(articleParam.getCategory().getId()));
            article.setCreateDate(System.currentTimeMillis());
            article.setCommentCounts(0);
            article.setSummary(articleParam.getSummary());
            article.setTitle(articleParam.getTitle());
            article.setViewCounts(0);
            article.setWeight(Article.Article_Common);
            articleMapper.insert(article);
        }

        //tags
        List<TagVo> tags = articleParam.getTags();
        if (tags != null) {
            for (TagVo tag : tags) {
                Long articleId = article.getId();
                if (isEdit) {
                    //先删除
                    LambdaQueryWrapper<ArticleTag> queryWrapper = Wrappers.lambdaQuery();
                    queryWrapper.eq(ArticleTag::getArticleId, articleId);
                    articleTagMapper.delete(queryWrapper);
                }
                ArticleTag articleTag = new ArticleTag();
                articleTag.setArticleId(articleId);
                articleTag.setTagId(Long.parseLong(tag.getId()));
                articleTagMapper.insert(articleTag);
            }
        }

        //body
        if (isEdit) {
            ArticleBody articleBody = new ArticleBody();
            articleBody.setArticleId(article.getId());
            articleBody.setContent(articleParam.getBody().getContent());
            articleBody.setContentHtml(articleParam.getBody().getContentHtml());
            LambdaUpdateWrapper<ArticleBody> updateWrapper = Wrappers.lambdaUpdate();
            updateWrapper.eq(ArticleBody::getArticleId, article.getId());
            articleBodyMapper.update(articleBody, updateWrapper);
        } else {
            ArticleBody articleBody = new ArticleBody();
            articleBody.setContent(articleParam.getBody().getContent());
            articleBody.setContentHtml(articleParam.getBody().getContentHtml());
            articleBody.setArticleId(article.getId());
            articleBodyMapper.insert(articleBody);
            article.setBodyId(articleBody.getId());
            articleMapper.updateById(article);
        }

        Map<String, String> map = new HashMap<>();
        map.put("id", article.getId().toString());

        return Result.success(map);
    }

    private List<ArticleVo> copyList(List<Article> articles, boolean isTags, boolean isAuthor) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article article : articles) {
            articleVoList.add(copy(article, isTags, isAuthor, false, false));
        }
        return articleVoList;
    }

    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor, boolean isBody) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record, isTag, isAuthor, isBody, false));
        }
        return articleVoList;
    }

    private List<ArticleVo> copyList(List<Article> records, boolean isTag, boolean isAuthor, boolean isBody, boolean isCategory) {
        List<ArticleVo> articleVoList = new ArrayList<>();
        for (Article record : records) {
            articleVoList.add(copy(record, isTag, isAuthor, isBody, isCategory));
        }
        return articleVoList;
    }

    private ArticleVo copy(Article article, boolean isTag, boolean isAuthor, boolean isBody, boolean isCategory) {
        ArticleVo articleVo = new ArticleVo();
        articleVo.setId(String.valueOf(article.getId()));
        BeanUtils.copyProperties(article, articleVo);
        articleVo.setCreateDate(new DateTime(article.getCreateDate()).toString("yyyy-MM-dd HH:mm"));
        //并不是所有的接口 都需要标签 ，作者信息
        if (isTag) {
            List<TagVo> tags = tagsService.findTagsByArticleId(article.getId());
            articleVo.setTags(tags);
        }
        if (isAuthor) {
            SysUser sysUser = sysUserService.findSysUserById(article.getAuthorId());
            UserVo userVo = new UserVo();
            userVo.setAvatar(sysUser.getAvatar());
            userVo.setId(String.valueOf(sysUser.getId()));
            userVo.setNickname(sysUser.getNickname());
            articleVo.setAuthor(userVo);
        }
        if (isBody) {
            ArticleBodyVo articleBody = findArticleBody(article.getId());
            articleVo.setBody(articleBody);
        }
        if (isCategory) {
            CategoryVo categoryVo = findCategory(article.getCategoryId());
            articleVo.setCategory(categoryVo);
        }
        return articleVo;
    }

    private CategoryVo findCategory(Long categoryId) {
        return categoryService.findCategoryById(categoryId);
    }

    private ArticleBodyVo findArticleBody(Long articleId) {
        LambdaQueryWrapper<ArticleBody> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ArticleBody::getArticleId, articleId);
        ArticleBody articleBody = articleBodyMapper.selectOne(queryWrapper);
        ArticleBodyVo articleBodyVo = new ArticleBodyVo();
        articleBodyVo.setContent(articleBody.getContent());
        return articleBodyVo;
    }
}
