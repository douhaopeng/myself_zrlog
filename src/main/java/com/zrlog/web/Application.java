package com.zrlog.web;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;

import javax.servlet.ServletException;
import java.io.File;

public class Application {
    public static void main(String[] args) throws ServletException, LifecycleException {
        String webappDirLocation = "src/main/webapp";
        Tomcat tomcat = new Tomcat();
        String webPort = System.getenv("PORT");
        if(webPort==null || webPort.isEmpty()){
            webPort = "8080";
        }
        tomcat.setPort(Integer.valueOf(webPort));
        File additionWebInfClasses = new File("target/classes");
        tomcat.setBaseDir(additionWebInfClasses.toString());
        tomcat.addWebapp("", new File(webappDirLocation).getAbsolutePath());
        tomcat.start();
        tomcat.getServer().await();

    }
}
