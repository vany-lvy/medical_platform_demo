package com.bezkoder.spring.datajpa;

import java.sql.*;

public class myDatabase {
    public Connection getConnection()
    {
        Connection conn = null;   //数据库连接
        try
        {
            //Class.forName("com.postgresql.jdbc.Driver"); //加载数据库驱动，注册到驱动管理器
            /*数据库链接地址*/
            String url = "jdbc:postgresql://localhost:5432/health_platform?useSSL=false";
            String username = "postgre";
            String password = "hellodoge";
            /*创建Connection链接*/
            conn = DriverManager.getConnection(url, username, password);

        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return conn;  //返回数据库连接
    }

}
