package com.zhao.seckill.utils;

import com.zhao.seckill.domain.pojo.User;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author noblegasesgoo
 * @version 0.0.1
 * @date 2022/2/15 18:36
 * @description user工具类
 */

@Component
public class UserUtil {

    public void createUserAndToken(int count) throws Exception {

        /** 打开io **/
        File file = new File("D:\\DevelopmentEnvironment\\JMeter\\config\\config.txt");
        if(!file.exists()){
            file.createNewFile();
        }
        FileWriter fileWriter = new FileWriter("D:\\DevelopmentEnvironment\\JMeter\\config\\config.txt",true);

        /** 生成用户数据 **/
        List<User> users = new ArrayList<User>();
        for (int i = 0; i < count; i++) {

            User user = new User();

            user.setRegisterDate(new Date());
            user.setId(1000000000L + i);
            user.setNickname("user" + i);
            user.setPassword(MD5Util.inputPasswordToDBPassword("123456"));
            user.setSalt("ZlmWl1314@#*&^!@");
            user.setLoginCount(0);

            users.add(user);
            String token = JWTUtil.createToken(user.getId());

            /** 顺便存入config.txt **/
            fileWriter.write(user.getId() + "," + "123456" + "," + token + "\n");
        }

        /** 打开数据库连接 **/
        Connection connection = getConnection();
        String sql = "insert into goo_user(login_count,nickname,register_date,salt,password,id)" +
                "values(?,?,?,?,?,?)";
        PreparedStatement preparedStatement = connection.prepareStatement(sql);

        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            preparedStatement.setInt(1, user.getLoginCount());
            preparedStatement.setString(2, user.getNickname());
            preparedStatement.setTimestamp(3, new Timestamp(user.getRegisterDate().getTime()));;
            preparedStatement.setString(4, user.getSalt());
            preparedStatement.setString(5, user.getPassword());
            preparedStatement.setLong(6, user.getId());

            preparedStatement.addBatch();
        }

        /** 插入数据 **/
        preparedStatement.executeBatch();

        /** 关闭所有io **/
        preparedStatement.close();
        connection.close();
        fileWriter.close();
    }

    private Connection getConnection() throws Exception {

        String url = "jdbc:mysql://localhost:3306/seckill?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai";
        String username = "root";
        String password = "mysql123456";
        String driver = "com.mysql.cj.jdbc.Driver";

        Class.forName(driver);

        return DriverManager.getConnection(url, username, password);
    }

    public void main(String[] args) throws Exception {
        createUserAndToken(5000);
    }
}
