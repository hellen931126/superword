/*
 * APDPlat - Application Product Development Platform
 * Copyright (c) 2013, 杨尚川, yang-shangchuan@qq.com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.apdplat.superword.tools;


import org.apdplat.superword.model.UserText;
import org.apdplat.superword.model.UserWord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 追踪用户查词记录
 * @author 杨尚川
 */
public class MySQLUtils {
    private static final Logger LOG = LoggerFactory.getLogger(MySQLUtils.class);

    private static final String DRIVER = "com.mysql.jdbc.Driver";
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/superword?useUnicode=true&characterEncoding=utf8";
    private static final String USER = "root";
    private static final String PASSWORD = "root";

    static {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            LOG.error("MySQL驱动加载失败：", e);
        }
    }

    private MySQLUtils() {
    }

    public static List<UserText> getHistoryUseTextsFromDatabase(String userName) {
        List<UserText> userTexts = new ArrayList<>();
        String sql = "select text,date_time from user_text where user_name=?";
        Connection con = getConnection();
        if(con == null){
            return userTexts;
        }
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = con.prepareStatement(sql);
            pst.setString(1, userName);
            rs = pst.executeQuery();
            while (rs.next()) {
                String text = rs.getString(1);
                Timestamp timestamp = rs.getTimestamp(2);
                UserText userText = new UserText();
                userText.setText(text);
                userText.setDateTime(new java.util.Date(timestamp.getTime()));
                userText.setUserName(userName);
                userTexts.add(userText);
            }
        } catch (SQLException e) {
            LOG.error("查询失败", e);
        } finally {
            close(con, pst, rs);
        }
        return userTexts;
    }

    public static List<UserWord> getHistoryUserWordsFromDatabase(String userName) {
        List<UserWord> userWords = new ArrayList<>();
        String sql = "select word,dictionary,date_time from user_word where user_name=?";
        Connection con = getConnection();
        if(con == null){
            return userWords;
        }
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = con.prepareStatement(sql);
            pst.setString(1, userName);
            rs = pst.executeQuery();
            while (rs.next()) {
                String word = rs.getString(1);
                String dictionary = rs.getString(2);
                Timestamp timestamp = rs.getTimestamp(3);
                UserWord userWord = new UserWord();
                userWord.setWord(word);
                userWord.setDictionary(dictionary);
                userWord.setDateTime(new java.util.Date(timestamp.getTime()));
                userWord.setUserName(userName);
                userWords.add(userWord);
            }
        } catch (SQLException e) {
            LOG.error("查询失败", e);
        } finally {
            close(con, pst, rs);
        }
        return userWords;
    }

    public static void saveUserTextToDatabase(UserText userText) {
        String sql = "insert into user_text (user_name, text, date_time) values (?, ?, ?)";
        Connection con = getConnection();
        if(con == null){
            return ;
        }
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = con.prepareStatement(sql);
            pst.setString(1, userText.getUserName());
            pst.setString(2, userText.getText());
            pst.setTimestamp(3, new Timestamp(userText.getDateTime().getTime()));
            pst.executeUpdate();
        } catch (SQLException e) {
            LOG.error("保存失败", e);
        } finally {
            close(con, pst, rs);
        }
    }

    public static void saveUserWordToDatabase(UserWord userWord) {
        String sql = "insert into user_word (user_name, word, dictionary, date_time) values (?, ?, ?, ?)";
        Connection con = getConnection();
        if(con == null){
            return ;
        }
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            pst = con.prepareStatement(sql);
            pst.setString(1, userWord.getUserName());
            pst.setString(2, userWord.getWord());
            pst.setString(3, userWord.getDictionary());
            pst.setTimestamp(4, new Timestamp(userWord.getDateTime().getTime()));
            pst.executeUpdate();
        } catch (SQLException e) {
            LOG.error("保存失败", e);
        } finally {
            close(con, pst, rs);
        }
    }

    public static Connection getConnection() {
        Connection con = null;
        try {
            con = DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            LOG.error("MySQL获取数据库连接失败：", e);
        }
        return con;
    }

    public static void close(Statement st) {
        close(null, st, null);
    }

    public static void close(Statement st, ResultSet rs) {
        close(null, st, rs);
    }

    public static void close(Connection con, Statement st, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
                rs = null;
            }
            if (st != null) {
                st.close();
                st = null;
            }
            if (con != null) {
                con.close();
                con = null;
            }
        } catch (SQLException e) {
            LOG.error("数据库关闭失败", e);
        }
    }

    public static void close(Connection con, Statement st) {
        close(con, st, null);
    }

    public static void close(Connection con) {
        close(con, null, null);
    }

    public static void main(String[] args) throws Exception {
        UserWord userWord = new UserWord();
        userWord.setDateTime(new Date(System.currentTimeMillis()));
        userWord.setWord("fabulous");
        userWord.setUserName("ysc");
        MySQLUtils.saveUserWordToDatabase(userWord);

        System.out.println(MySQLUtils.getHistoryUserWordsFromDatabase("ysc"));
    }
}