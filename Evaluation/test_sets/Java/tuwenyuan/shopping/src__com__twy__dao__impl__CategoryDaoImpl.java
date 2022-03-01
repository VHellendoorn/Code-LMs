package com.twy.dao.impl;

import java.sql.SQLException;
import java.util.List;

import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanHandler;
import org.apache.commons.dbutils.handlers.BeanListHandler;

import com.twy.dao.CategoryDao;
import com.twy.domain.Category;
import com.twy.exception.DaoException;
import com.twy.util.DBCPUtil;

public class CategoryDaoImpl implements CategoryDao {
	private QueryRunner qr = new QueryRunner(DBCPUtil.getDataSource());

	public void addCategory(Category c) {
		try {
			qr.update(
					"insert into category (id,name,description) values(?,?,?)",
					c.getId(), c.getName(), c.getDescription());
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DaoException(e);
		}
	}

	public List<Category> findAllCategories() {
		try {
			return qr.query("select * from category",
					new BeanListHandler<Category>(Category.class));
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DaoException(e);
		}
	}

	public Category findCategoryById(String categoryId) {
		try {
			return qr.query("select * from category where id=?",
					new BeanHandler<Category>(Category.class),categoryId);
		} catch (SQLException e) {
			e.printStackTrace();
			throw new DaoException(e);
		}
	}

}
