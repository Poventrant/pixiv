package dao;


import models.Pixiv;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PixivDao extends BaseDao<Pixiv> {

    public String[] getAuthors(boolean moren) {
        String sqlName = namespace + ".selectBySql";
        List<String> res = null;
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("sql", "select p.author from pixiv p group by p.author");
            res = sqlMapClient.queryForList(sqlName, map);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        if (moren) {
            String strs[] = new String[res.size() + 1];
            strs[0] = "默认";
            for (int i = 0; i < res.size(); ++i) {
                strs[i + 1] = res.get(i);
            }
            return strs;
        } else {
            String strs[] = new String[res.size()];
            for (int i = 0; i < res.size(); ++i) {
                strs[i] = res.get(i);
            }
            return strs;
        }
    }

    public void deleteByAuthor(String author) throws Exception {
        String sqlName = namespace + ".deleteBySql";
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("sql", "delete from pixiv where author = '" + author + "'");
            sqlMapClient.delete(sqlName, map);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


    public void deleteAll() {
        String sqlName = namespace + ".deleteBySql";
        try {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("sql", "delete from pixiv where 1 = 1");
            sqlMapClient.delete(sqlName, map);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Pixiv> query(Map<String, Object> queryMap) {
        return super.query(queryMap);
    }

}
