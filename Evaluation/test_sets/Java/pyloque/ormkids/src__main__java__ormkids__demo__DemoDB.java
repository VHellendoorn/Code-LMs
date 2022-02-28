package ormkids.demo;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;

import ormkids.KidsException;
import ormkids.DB;
import ormkids.IEntity;
import ormkids.Meta;

public class DemoDB extends DB {

	private DataSource ds;

	public DemoDB(String name, String uri) {
		this(name, new HashMap<>(), uri);
	}

	public DemoDB(String name, Map<Class<? extends IEntity>, Meta> metas, String uri) {
		super(name, metas);
		var ds = new MysqlConnectionPoolDataSource();
		ds.setUrl(uri);
		this.ds = ds;
	}

	@Override
	protected Connection conn() {
		try {
			return ds.getConnection();
		} catch (SQLException e) {
			throw new KidsException(e);
		}
	}

}
