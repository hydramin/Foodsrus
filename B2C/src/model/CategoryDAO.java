package model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class CategoryDAO {
	public static List<CategoryBean> retrieve(String prefix) throws Exception
	{
		Class.forName("org.apache.derby.jdbc.ClientDriver").newInstance();
		Connection conn = DriverManager.getConnection("jdbc:derby://localhost:64413/EECS;user=student;password=secret");
		PreparedStatement s = null;
		ResultSet r = null;
		try
		{
			s = conn.prepareStatement("set schema roumani");
			s.executeUpdate();
			s.close();
			String pre;
			if (prefix == null)
			{
				pre = "";
			}
			else
			{
				pre = prefix;
			}
			String sql = "SELECT * FROM CATEGORY WHERE NAME LIKE ?";
					
			s = conn.prepareStatement(sql);
			s.setString(1, "%"+pre+"%");
			
			r = s.executeQuery();
			
			List<CategoryBean> result = new ArrayList<CategoryBean>();
			while(r.next())
			{
				CategoryBean bean = new CategoryBean(r.getInt("ID"), r.getString("NAME"), r.getString("DESCRIPTION"), r.getBlob("PICTURE"));
				result.add(bean);
			}
			r.close(); s.close(); conn.close();
			return result;
		}
		catch (Exception e)
		{
			throw e;
		}
	}
	
}
