package dao;

import com.ibatis.sqlmap.client.SqlMapClient;
import dao.conf.SqlConfigLoader;
import play.db.Model;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BaseDao<T extends Model> {

	protected Class<T> t;
	protected String namespace;

	protected static SqlMapClient sqlMapClient = SqlConfigLoader.getSqlMapClient();

	public BaseDao() {
		Class c = getClass();
		Type t = c.getGenericSuperclass();
		System.out.println(t);
		if (t instanceof ParameterizedType) {
			Type[] p = ((ParameterizedType) t).getActualTypeArguments();
			this.t = (Class<T>) p[0];
		}
		this.namespace = this.t.getName();
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public int create(T o) {
		String sqlName = namespace + ".create";
		Object ret = null;
		try {
			ret = sqlMapClient.insert(sqlName, o);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return (Integer) (ret == null ? 0 : ret);
	}

	public int batchCreate(Collection<T> c) {
		String sqlName = namespace + ".create";
		int ret = 0;
		if (c == null || c.size() == 0)
			return ret;
		try {
			sqlMapClient.startBatch();
			for (T o : c) {
				sqlMapClient.insert(sqlName, o);
			}
			ret = sqlMapClient.executeBatch();
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	public <T1> int delete(T1 id) {
		String sqlName = namespace + ".delete";
		return delete(sqlName, id);
	}

	public int delete(T o) {
		String sqlName = namespace + ".deleteByDO";
		return delete(sqlName, o);
	}

	protected int delete(String sqlName, Object params) {
		int ret = 0;
		try {
			ret = sqlMapClient.delete(sqlName, params);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	public int update(T o) {
		String sqlName = namespace + ".update";
		int ret = 0;
		try {
			ret = sqlMapClient.update(sqlName, o);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return ret;
	}

	public int batchUpdate(Collection<T> c) {
		String sqlName = namespace + ".update";
		int ret = 0;
		if (c == null || c.size() == 0)
			return ret;
		try {
			sqlMapClient.startBatch();
			for (T o : c) {
				sqlMapClient.update(sqlName, o);
			}
			sqlMapClient.executeBatch();
			ret = 1;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	public int batchUpdateMap(Collection<Map> batchMap) {
		String sqlName = namespace + ".updateMap";
		int ret = 0;
		if (batchMap == null || batchMap.size() == 0)
			return ret;
		try {
			sqlMapClient.startBatch();
			for (Map m : batchMap) {
				sqlMapClient.update(sqlName, m);
			}
			sqlMapClient.executeBatch();
			ret = 1;
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	public int update(Map<String, Object> updateMap) {
		String sqlName = namespace + ".updateMap";
		int ret = 0;
		if (updateMap == null || updateMap.isEmpty()) {
			return ret;
		}
		try {
			ret = sqlMapClient.update(sqlName, updateMap);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	@SuppressWarnings("hiding")
	public <T> T find(Integer id) {
		String sqlName = namespace + ".find";

		T ret = null;
		try {
			ret = (T) sqlMapClient.queryForObject(sqlName, id);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	public List<T> find(String[] fieldNames, Object[] fieldValues) throws Exception {
		if (fieldNames.length != fieldValues.length)
			throw new Exception("参数不匹配");
		Map<String, Object> queryMap = new HashMap<String, Object>();
		for (int i = 0; i < fieldNames.length; i++)
			queryMap.put(fieldNames[i], fieldValues[i]);
		return query(queryMap);

	}

	public List<T> find(String fieldName, Object fieldValue) throws Exception {
		return find(new String[] { fieldName }, new Object[] { fieldValue });

	}

	protected T find(String sqlName, Map<String, Object> queryMap) {
		T ret = null;
		try {
			ret = (T) sqlMapClient.queryForObject(sqlName, queryMap);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
		return ret;
	}

	public List<T> query(Map<String, Object> queryMap) {
		String sqlName = namespace + ".query";
		List<T> ret = null;
		try {
			ret = (List<T>) sqlMapClient.queryForList(sqlName, queryMap);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return ret;
	}

	public List<T> query(String sqlName, Map<String, Object> params) {
		List<T> ret = null;
		try {
			ret = (List<T>) sqlMapClient.queryForList(sqlName, params);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return ret;
	}

	public int count(String sqlName, Map<String, Object> queryMap) {
		int ret = 0;
		try {
			ret = (Integer) sqlMapClient.queryForObject(sqlName, queryMap);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return ret;
	}

	public int count(Map<String, Object> queryMap) {
		String sqlName = namespace + ".count";
		int ret = 0;
		try {
			ret = (Integer) sqlMapClient.queryForObject(sqlName, queryMap);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}

		return ret;
	}

	public String getStatmentNameBySqlId(String id) {
		return this.namespace + "." + id;
	}

}
