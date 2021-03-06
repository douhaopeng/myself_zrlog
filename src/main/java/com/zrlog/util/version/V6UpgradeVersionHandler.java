package com.zrlog.util.version;

import com.zrlog.service.ArticleService;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class V6UpgradeVersionHandler implements UpgradeVersionHandler{
    @Override
    public void doUpgrade(Connection connection) throws Exception {
        Thread.sleep(10000L);
        PreparedStatement ps = connection.prepareStatement("select content,logid from log");
        ResultSet rs = ps.executeQuery();
        while (rs.next()) {
            int id = rs.getInt("logid");
            PreparedStatement ups = connection.prepareStatement("update log set search_content = ? where logid = ?");
            ups.setString(1, (String) new ArticleService().getPlainSearchTxt(rs.getString("content")));
            ups.setInt(2, id);
            ups.execute();
        }
    }
}
