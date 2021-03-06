package com.lxinet.jeesns.service.cms.impl;

import com.lxinet.jeesns.core.dto.ResponseModel;
import com.lxinet.jeesns.core.model.Page;
import com.lxinet.jeesns.core.utils.StringUtils;
import com.lxinet.jeesns.dao.cms.IArticleCommentDao;
import com.lxinet.jeesns.model.cms.Article;
import com.lxinet.jeesns.model.cms.ArticleComment;
import com.lxinet.jeesns.model.member.Member;
import com.lxinet.jeesns.service.cms.IArticleCommentService;
import com.lxinet.jeesns.service.cms.IArticleService;
import com.lxinet.jeesns.service.member.IScoreDetailService;
import com.lxinet.jeesns.service.system.IActionLogService;
import com.lxinet.jeesns.common.utils.ActionUtil;
import com.lxinet.jeesns.common.utils.ScoreRuleConsts;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * Created by zchuanzhao on 2016/10/14.
 */
@Service("articleCommentService")
public class ArticleCommentServiceImpl implements IArticleCommentService {
    @Resource
    private IArticleCommentDao articleCommentDao;
    @Resource
    private IArticleService articleService;
    @Resource
    private IActionLogService actionLogService;
    @Resource
    private IScoreDetailService scoreDetailService;

    @Override
    public ArticleComment findById(int id) {
        return articleCommentDao.findById(id);
    }

    @Override
    public ResponseModel save(Member loginMember, String content, Integer articleId) {
        Article article = articleService.findById(articleId);
        if(article == null){
            return new ResponseModel(-1,"文章不存在");
        }
        if(StringUtils.isEmpty(content)){
            return new ResponseModel(-1,"内容不能为空");
        }
        ArticleComment articleComment = new ArticleComment();
        articleComment.setMemberId(loginMember.getId());
        articleComment.setArticleId(articleId);
        articleComment.setContent(content);
        int result = articleCommentDao.save(articleComment);
        if(result == 1){
            //文章评论奖励
            scoreDetailService.scoreBonus(loginMember.getId(), ScoreRuleConsts.ARTICLE_REVIEWS,articleComment.getId());
            return new ResponseModel(1,"评论成功");
        }else {
            return new ResponseModel(-1,"评论失败");
        }
    }

    @Override
    public ResponseModel listByArticle(Page page, int articleId) {
        List<ArticleComment> list = articleCommentDao.listByArticle(page, articleId);
        ResponseModel model = new ResponseModel(0,page);
        model.setData(list);
        return model;
    }

    @Override
    public void deleteByArticle(Integer articleId) {
        articleCommentDao.deleteByArticle(articleId);
    }

    @Override
    @Transactional
    public ResponseModel delete(Member loginMember, int id) {
        ArticleComment articleComment = this.findById(id);
        if(articleComment == null){
            return new ResponseModel(-1,"评论不存在");
        }
        int result = articleCommentDao.delete(id);
        if(result == 1){
            //扣除积分
            scoreDetailService.scoreCancelBonus(loginMember.getId(), ScoreRuleConsts.ARTICLE_REVIEWS,id);
            actionLogService.save(loginMember.getCurrLoginIp(),loginMember.getId(), ActionUtil.DELETE_ARTICLE_COMMENT,"ID："+articleComment.getId()+"，内容："+articleComment.getContent());
            return new ResponseModel(1,"删除成功");
        }
        return new ResponseModel(-1,"删除失败");
    }

}
