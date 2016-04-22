package services;

import dao.BaseDao;
import junit.framework.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.Model;
import utils.BeanUtils;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public abstract class BaseService<T extends Model> {

	private final Logger logger = LoggerFactory.getLogger(BaseService.class);

	protected BaseDao<T> dao;

	private Class<T> entityClass;

	public BaseService() {
		super();
		try {
			Object genericClz = getClass().getGenericSuperclass();
			if (genericClz instanceof ParameterizedType) {
				entityClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
						.getActualTypeArguments()[0];
			}
		} catch (Exception e) {
			logger.error("error detail:", e);
		}
	}

	protected void preCreate(T entity) {

	}

	protected void preUpdate(T entity) {

	}

	public T create(T entity) {
		int ret = dao.create(entity);
		return entity;
	}

	public int batchCreate(Collection<T> c) {
		logger.debug("batchCreate...");
		return dao.batchCreate(c);
	}

	public int delete(int id) {
		logger.debug("delete...");
		int ret = dao.delete(id);
		return ret;
	}

	public int delete(T entity) {
		logger.debug("delete...");
		int ret = dao.delete(entity);
		return ret;
	}

	public T update(T entity) {
		int ret = dao.update(entity);
		return entity;
	}

	public int batchUpdate(Collection<T> c) {
		logger.debug("batchUpdate...");
		int ret = dao.batchUpdate(c);
		return ret;
	}

	public int update(Map<String, Object> updateMap) {
		logger.debug("update...");
		int ret = dao.update(updateMap);
		return ret;
	}

	public T find(Integer id) {
		Assert.assertNotNull(id);
		return dao.find(id);
	}

	public T findUnique(T entity) {
		List<T> list = queryBy(null, entity);
		if (null != list && list.size() > 0)
			return list.get(0);
		return null;
	}

	public List<T> query(Map<String, Object> queryMap) {
		return dao.query(queryMap);
	}

	public List<T> queryBy(String sqlName, T entity) {
		Map<String, Object> queryMap = new HashMap<String, Object>();
		List<Field> fields = BeanUtils.getDeclaredFields(entity.getClass());

		for (Field field : fields) {
			try {
				Object value = BeanUtils.getFieldValue(entity, field.getName());
				if (value == null)
					continue;
				queryMap.put(field.getName(), value);
			} catch (Exception e) {
				logger.error("error detail:", e);
			}
		}
		if (null == sqlName)
			return dao.query(queryMap);
		else
			return dao.query(sqlName, queryMap);
	}
	
	public List queryByVO(String sqlName, Object entity) {
		Map<String, Object> queryMap = new HashMap<String, Object>();
		List<Field> fields = BeanUtils.getDeclaredFields(entity.getClass());

		for (Field field : fields) {
			try {
				Object value = BeanUtils.getFieldValue(entity, field.getName());
				if (value == null)
					continue;
				queryMap.put(field.getName(), value);
			} catch (Exception e) {
				logger.error("error detail:", e);
			}
		}
		if (null == sqlName)
			return dao.query(queryMap);
		else
			return dao.query(sqlName, queryMap);
	}

	public List<T> queryAll() {
		return dao.query(null);
	}

	public int count(Map<String, Object> queryMap) {
		return dao.count(queryMap);
	}

}
