package com.bezkoder.spring.datajpa;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import java.util.ResourceBundle;
import javax.validation.constraints.Null;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {
    public Connection getConnection() {
        Connection conn = null;   //数据库连接
        try
        {
//            ResourceBundle resource = ResourceBundle.getBundle("dbconn");//test为属性文件名，放在包com.mmq下，如果是放在src下，直接用test即可
//            String username = resource.getString("username");
//            String password = resource.getString("password");
//            String url = resource.getString("url");
            String url = "jdbc:postgresql://localhost:5432/health_platform?useSSL=true";
            String username = "postgres";
            String password = "hellodoge";

            conn = DriverManager.getConnection(url, username, password);
        }
        catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return conn;  //返回数据库连接
    }

    public ResultSet getQuery(String ps_string, String... ps_params){
        try {
            Connection conn = getConnection();
            PreparedStatement ps;
            ResultSet rs;
            ps = conn.prepareStatement(ps_string);
            for(int i=0; i<ps_params.length;++i){
                ps.setString(i+1,ps_params[i]);
            }
            rs = ps.executeQuery();
            return rs;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public int getUpdate(String ps_string, String... ps_params){
        try {
            Connection conn = getConnection();
            PreparedStatement ps;
            ResultSet rs;
            ps = conn.prepareStatement(ps_string);
            for(int i=0; i<ps_params.length;++i){
                ps.setString(i+1,ps_params[i]);
            }
            rs = ps.executeQuery();
            return 0;
        } catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }

    public Boolean checkUserandToken(String role, String telephone, String token){
        Connection conn = getConnection();
        try
        {
            PreparedStatement ps;
            ResultSet rs;
            if(role.equals("Patient")){
                ps = conn.prepareStatement("select * from patient_tel where tel_num = (?) and token = (?)");
            } else{
                ps = conn.prepareStatement("select * from doctor_tel where tel_num = (?) and token = (?)");
            }
            ps.setString(1,telephone);
            ps.setString(2,token);
            rs = ps.executeQuery();
            if(rs.next()){
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }
        }catch(Exception e)
        {
            e.printStackTrace();
            return Boolean.FALSE;
        }
    }

    public String getAuth(String patient_tel){
        String resultjson = "";
        Connection conn = getConnection();  //创建数据库连接
        try
        {
            //调用存储过程
            PreparedStatement ps = conn.prepareStatement("select tel_num, access_right from doctor_tel where access_right = ?");
            ps.setString(1, patient_tel);
            ResultSet rs = ps.executeQuery(); //执行查询操作，并获取结果集
            resultjson = resultSetToJson(rs);

        }catch(Exception e)
        {
            e.printStackTrace();
        }//返回list
        return resultjson;     //返回list
    }

    public String findAll(String telephone)
    {
        String resultjson = "";
        Connection conn = getConnection();  //创建数据库连接
        try
        {
            //调用存储过程
            PreparedStatement ps = conn.prepareStatement("select * from gettotalinfobytelephone(?)");
            ps.setString(1, telephone);
            ResultSet rs = ps.executeQuery(); //执行查询操作，并获取结果集

            resultjson = resultSetToJson(rs);

        }catch(Exception e)
        {
            e.printStackTrace();
        }//返回list
        return resultjson;     //返回list
    }

    public String getRights(String telephone){
        Connection conn = getConnection();  //创建数据库连接
        try
        {
            //调用存储过程
            PreparedStatement ps = conn.prepareStatement("select access_right from doctor_tel where access_right is not null and tel_num = ?");
            ps.setString(1, telephone);
            ResultSet rs = ps.executeQuery(); //执行查询操作，并获取结果集
            if(rs.next()){
                String rights = rs.getString("access_right");
                return rights;
            } else {
                return "";
            }
        }
        catch(Exception e)
        {
            e.printStackTrace();
            return "";
        }//返回list
    }



    public void setRights(String patient_tel, String doctor_tel, String report_id){
        Connection conn = getConnection();  //创建数据库连接
        try
        {
            //调用存储过程
            PreparedStatement ps = conn.prepareStatement("update doctor_tel set access_right = ? where tel_num = ?");
            ps.setString(1, patient_tel);
            ps.setString(2, doctor_tel);
            ResultSet rs = ps.executeQuery(); //执行查询操作，并获取结果集
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }//返回list
    }

    public void unsetRights(String doctor_tel){
        Connection conn = getConnection();  //创建数据库连接
        try
        {
            //调用存储过程
            PreparedStatement ps = conn.prepareStatement("update doctor_tel set access_right = null where tel_num = ?");
            ps.setString(1, doctor_tel);
            ResultSet rs = ps.executeQuery(); //执行查询操作，并获取结果集
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }//返回list
    }

    public void updateinfo(String newtele, String oldtele , String newtoken, String oldtoken){
        Connection conn = getConnection();  //创建数据库连接
        try
        {
            //调用存储过程
            PreparedStatement ps = conn.prepareStatement("select tel_num from patient_tel where tel_num = ?");
            ps.setString(1,oldtele);
            //ps.setString(2,token);
            ResultSet rs = ps.executeQuery(); //执行查询操作，并获取结果集

            if(rs.next()){
                PreparedStatement pss = conn.prepareStatement("update patient_tel set tel_num = ?, token = ? where tel_num = ? and token = ? ");
                pss.setString(1, newtele);
                pss.setString(2, newtoken);
                pss.setString(3, oldtele);
                pss.setString(4, oldtoken);

                //ps.setString(2,token);
                ResultSet rss = pss.executeQuery(); //执行查询操作，并获取结果//执行查询操作，并获取结果
            }

        }catch(Exception e)
        {
            e.printStackTrace();
        }//返回list
    }
    public void updateinfo2(String newtele, String oldtele){
        Connection conn = getConnection();  //创建数据库连接
        try
        {
            PreparedStatement psss = conn.prepareStatement("update patient_info set tel = ? where tel = ?");
            psss.setString(1, newtele);
            psss.setString(2, oldtele);
            ResultSet rsss = psss.executeQuery();

        }catch(Exception e)
        {
            e.printStackTrace();
        }//返回list
    }

    public ResponseEntity<String> VerifyTelephoneAndToken(String telephone, String token)
    {
        Connection conn = getConnection();  //创建数据库连接
        try
        {
            //调用存储过程
            PreparedStatement ps = conn.prepareStatement("select tel_num from patient_tel where tel_num = ?");
            ps.setString(1,telephone);
            //ps.setString(2,token);
            ResultSet rs = ps.executeQuery(); //执行查询操作，并获取结果集

            if(rs.next()){
                return new ResponseEntity<String>(findAll(telephone),HttpStatus.ACCEPTED);
            } else {
                return new ResponseEntity<String>("bad telephone number", HttpStatus.NOT_FOUND);
            }

        }catch(Exception e)
        {
            e.printStackTrace();
            return new ResponseEntity<String>("bad telephone number", HttpStatus.NOT_FOUND);

        }//返回list
    }


    public String resultSetToJson(ResultSet rs) throws SQLException, JSONException
    {
        // json数组
        JSONArray array = new JSONArray();

        // 获取列数
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        // 遍历ResultSet中的每条数据
        while (rs.next()) {
            JSONObject jsonObj = new JSONObject();

            // 遍历每一列
            for (int i = 1; i <= columnCount; i++) {
                String columnName =metaData.getColumnLabel(i);
                String value = rs.getString(columnName);
                jsonObj.put(columnName, value);
            }
            array.put(jsonObj);
        }

        return array.toString();
    }



}
