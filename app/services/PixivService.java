package services;

import dao.BaseDao;
import dao.PixivDao;
import models.Pixiv;
import utils.Page;
import utils.PageUtil;
import utils.PixivCrawler;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PixivService extends BaseService<Pixiv>{
    @Inject
    PixivDao dao;

    private static int PAGE_ITEMS = 20, PAGE_WIDTH = 7;

    protected BaseDao<Pixiv> getEntityDao() {
        return dao;
    }

    public String [] getAuthors(boolean moren) {
        return dao.getAuthors(moren);
    }

    public List<Pixiv> queryByPage(Map<String, String> paramsMap, Map<String, Object> returnMap) throws Exception {
        int currentPage = Integer.parseInt( paramsMap.get("page") );
        String mod = paramsMap.get("mod");
        String author = paramsMap.get("author");
        String sort = paramsMap.get("sort");

        Map<String, Object> queryMap = new HashMap<String, Object>();
        if(author != null && !author.equals("默认") ) {
            queryMap.put("author", author);
        }
        System.out.println("================================================" + currentPage);
        int totalRecords = dao.count(queryMap);
        Page page = new PageUtil().createPage(PAGE_ITEMS, currentPage, totalRecords, PAGE_WIDTH);
        if(currentPage > page.getTotalPage()) throw new Exception("页数错误");

        if(mod == null || mod.equals("authorType")) {
            if(sort != null && !sort.equals("0")) {
                if(sort.equals("1")) {  //按分数升序
                    queryMap.put("order_rate", "desc");
                } else if(sort.equals("2")) {  //大小排序
                    queryMap.put("order_size", "desc");
                } else if(sort.equals("3")) {  //创建时间排序
                    queryMap.put("order_createTime", "desc");
                }
            } else {
                queryMap.put("order_picid", "desc");    //默认情况下按照P在的图片ID排序
            }
        }

        queryMap.put("limit", page.getEveryPage());
        queryMap.put("offset", page.getBeginIndex());

        List<Pixiv> arps = dao.query(queryMap);

        returnMap.put("pageModel", page);

        return arps;

    }

    public void deleteByAuthor(String author) throws Exception{
        Matcher m = Pattern.compile(".*\\(([^)]*)").matcher(author);
        String authorId = null;
        if(m.find()) {
            authorId = m.group(1);
            if( !authorId.matches("-?\\d+(\\.\\d+)?") ) {
                throw new IllegalArgumentException("传递的作者信息错误。");   //再检查，防止SQL注入攻击
            }
        } else {
            throw new IllegalArgumentException("传递的作者信息错误。");
        }
        try {
            PixivCrawler.deleteAuthor(authorId);
            dao.deleteByAuthor(author);
        } catch (Exception e) {
            throw e;
        }
    }

    public void deleteAll() throws Exception {
        try {
            dao.deleteAll();
        } catch (Exception e) {
            throw e;
        }
    }




}
