package com.crazy.miaosha.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.crazy.miaosha.domain.MiaoshaUser;

/**
 *	生成用户到数据库
 *	和生成用户token到指定的（D:/tokens.txt）文件 
 *
 */
@SuppressWarnings("unused")
public class UserUtil {

	private static void createUser(int count) throws Exception{
		List<MiaoshaUser> users = new ArrayList<MiaoshaUser>(count);
		//生成用户
		for(int i = 0;i<count;i++){
			MiaoshaUser user = new MiaoshaUser();
			user.setId(13000000000L+i);
			user.setLoginCount(1);
			user.setNickname("user"+i);
			user.setRegisterDate(new Date());
			user.setSalt("abc123zxc456");
			user.setPassword(MD5Util.inputPassToDBPass("123456", user.getSalt()));
			users.add(user);
		}
		System.out.println("create user");
		//插入数据库
//		Connection conn = DBUtil.getConn();
//		String sql = "insert into miaosha_user(login_count,nickname,register_date,salt,password,id)values(?,?,?,?,?,?)";
//		PreparedStatement ps = conn.prepareStatement(sql);
//		for(int i = 0;i<users.size();i++){
//			MiaoshaUser user = users.get(i);
//			ps.setInt(1, user.getLoginCount());
//			ps.setString(2, user.getNickname());
//			ps.setTimestamp(3, new Timestamp(user.getRegisterDate().getTime()));
//			ps.setString(4, user.getSalt());
//			ps.setString(5, user.getPassword());
//			ps.setLong(6, user.getId());
//			ps.addBatch();
//		}
//		ps.executeBatch();
//		ps.close();
//		conn.close();
//		System.out.println("db insert");
		
		//登录，生成token
		String urlString = "http://localhost/8080/login/to_login";
		File file = new File("D:/tokens.txt");
		if(file.exists()){
			file.delete();
		}
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		file.createNewFile();
		raf.seek(0);
		for(int i=0;i<users.size();i++){
			MiaoshaUser user = users.get(i);
			URL url = new URL(urlString);
			HttpURLConnection co = (HttpURLConnection) url.openConnection();
			co.setRequestMethod("POST");
			co.setDoOutput(true);
			OutputStream out = co.getOutputStream();
			String params = "mobile="+user.getId()+"&password="+MD5Util.inputPassToFormPass("123456");
			out.write(params.getBytes());
			out.flush();
			InputStream inputStream = co.getInputStream();
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			byte buff[] = new byte[1024];
			int len = 0;
			while((len = inputStream.read(buff)) >= 0){
				bout.write(buff, 0, len);
			}
			inputStream.close();
			bout.close();
			String response = new String(bout.toByteArray());
			JSONObject jo = JSON.parseObject(response);
			String token = jo.getString("date");
			System.out.println("create token : " + user.getId());
			
			String row = user.getId() + "," + token;
			raf.seek(raf.length());
			raf.write(row.getBytes());
			raf.write("\r\n".getBytes());
			System.out.println("write to file : " + user.getId());
		}
		raf.close();
		
		System.out.println("over");
	}
	
	public static void main(String[] args) throws Exception {
		createUser(5000);
	}
	
}
