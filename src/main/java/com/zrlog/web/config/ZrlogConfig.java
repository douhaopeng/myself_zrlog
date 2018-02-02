package com.zrlog.web.config;

import com.hibegin.common.util.http.HttpUtil;
import com.hibegin.common.util.http.handle.HttpFileHandle;
import com.jfinal.config.*;
import com.jfinal.core.JFinal;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.IPlugin;
import com.jfinal.plugin.activerecord.ActiveRecordPlugin;
import com.jfinal.plugin.c3p0.C3p0Plugin;
import com.jfinal.render.ViewType;
import com.jfinal.template.Engine;
import com.zrlog.model.*;
import com.zrlog.util.BlogBuildInfoUtil;
import com.zrlog.util.ZrlogUtil;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Random;

public class ZrlogConfig extends JFinalConfig {
    private static final Logger LOGGER = Logger.getLogger(ZrlogConfig.class);
    //插件服务的下载地址
    private static final String PLUGIN_CORE_DOWNLOAD_URL = com.zrlog.common.Constants.ZRLOG_RESOURCE_DOWNLOAD_URL + "/plugin/core/plugin-core.jar";
    //存放Zrlog的一些系统参数
    private Properties systemProperties = new Properties();
    private Properties dbProperties = new Properties();
    // 读取系统参数
    private Properties systemProp = System.getProperties();
    //存放为config的属性，是为了安装完成后还获得JFinal的插件列表对象
    private Plugins plugins;

    public ZrlogConfig() {
        try {
            systemProperties.load(ZrlogConfig.class.getResourceAsStream("/zrlog.properties"));
        } catch (IOException e) {
            LOGGER.error("load systemProperties error", e);
        }
    }

    public static boolean isInstalled() {
        return new InstallService(PathKit.getWebRootPath() + "/WEB-INF").checkInstall();
    }

    private void runBlogPlugin(final String dbPropertiesPath, final String pluginJvmArgs) {
        //这里使用独立的线程进行启动，主要是为了防止插件服务出问题后，影响整体，同时是避免启动过慢的问题。
        new Thread() {
            @Override
            public void run() {
                PluginConfig.stopPluginCore();
                //加载 zrlog 提供的插件
                File pluginCoreFile = new File(PathKit.getWebRootPath() + "/WEB-INF/plugins/plugin-core.jar");
                if (!pluginCoreFile.exists()) {
                    pluginCoreFile.getParentFile().mkdirs();
                    String filePath = pluginCoreFile.getParentFile().toString();
                    try {
                        LOGGER.info("plugin-core.jar not exists will download from " + PLUGIN_CORE_DOWNLOAD_URL);
                        HttpUtil.getInstance().sendGetRequest(PLUGIN_CORE_DOWNLOAD_URL + "?_=" + System.currentTimeMillis(), new HashMap<String, String[]>(), new HttpFileHandle(filePath), new HashMap<String, String>());
                    } catch (IOException e) {
                        LOGGER.warn("download plugin core error", e);
                    }
                }
                int port = PluginConfig.pluginServerStart(pluginCoreFile, dbPropertiesPath, pluginJvmArgs, PathKit.getWebRootPath(), BlogBuildInfoUtil.getVersion());
                JFinal.me().getServletContext().setAttribute("pluginServerPort", port);
                JFinal.me().getServletContext().setAttribute("pluginServer", "http://localhost:" + port);
            }
        }.start();

    }

    private ActiveRecordPlugin getActiveRecordPlugin(C3p0Plugin c3p0Plugin) {
        ActiveRecordPlugin arp = new ActiveRecordPlugin("c3p0Plugin" + new Random().nextInt(), c3p0Plugin);
        arp.addMapping("user", "userId", User.class);
        arp.addMapping("log", "logId", Log.class);
        arp.addMapping("type", "typeId", Type.class);
        arp.addMapping("link", "linkId", Link.class);
        arp.addMapping("comment", "commentId", Comment.class);
        arp.addMapping("lognav", "navId", LogNav.class);
        arp.addMapping("website", "siteId", WebSite.class);
        arp.addMapping("plugin", "pluginId", Plugin.class);
        arp.addMapping("tag", "tagId", Tag.class);
        return arp;
    }

    @Override
    public void configConstant(Constants con) {
        con.setDevMode(BlogBuildInfoUtil.isDev());
        con.setViewType(ViewType.JSP);
        con.setEncoding("utf-8");
        con.setI18nDefaultBaseName(com.zrlog.common.Constants.I18N);
        con.setI18nDefaultLocale("zh_CN");
        con.setError404View("/error/404.html");
        con.setError500View("/error/500.html");
        con.setError403View("/error/403.html");
        con.setBaseUploadPath(PathKit.getWebRootPath() + com.zrlog.common.Constants.ATTACHED_FOLDER);
    }

    @Override
    public void configRoute(Routes routes) {

        // 添加浏览者能访问Control 路由
        routes.add("/post", PostController.class);
        routes.add("/api", APIController.class);
        routes.add("/", PostController.class);
        routes.add("/install", InstallController.class);
        // 后台管理者
        routes.add(new AdminRoutes());
    }

    @Override
    public void configEngine(Engine engine) {

    }

    @Override
    public void configPlugin(Plugins plugins) {
        FileInputStream in = null;
        try {
            // 如果没有安装的情况下不初始化数据
            if (isInstalled()) {
                String dbPropertiesFile = PathKit.getWebRootPath() + "/WEB-INF/db.properties";
                try {
                    in = new FileInputStream(dbPropertiesFile);
                    dbProperties.load(in);
                } catch (IOException e) {
                    LOGGER.error("load db.systemProperties error", e);
                }
                systemProp.put("dbServer.version", ZrlogUtil.getDatabaseServerVersion(dbProperties.getProperty("jdbcUrl"), dbProperties.getProperty("user"),
                        dbProperties.getProperty("password"), dbProperties.getProperty("driverClass")));
                // 启动时候进行数据库连接
                C3p0Plugin c3p0Plugin = new C3p0Plugin(dbProperties.getProperty("jdbcUrl"),
                        dbProperties.getProperty("user"), dbProperties.getProperty("password"));
                plugins.add(c3p0Plugin);
                // 添加表与实体的映射关系
                plugins.add(getActiveRecordPlugin(c3p0Plugin));
                Object pluginJvmArgsObj = systemProperties.get("pluginJvmArgs");
                if (pluginJvmArgsObj == null) {
                    pluginJvmArgsObj = "";
                }
                runBlogPlugin(dbPropertiesFile, pluginJvmArgsObj.toString());
                plugins.add(new UpdateVersionPlugin());
            }
        } catch (Exception e) {
            LOGGER.warn("configPlugin exception ", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    LOGGER.error("close stream error", e);
                }
            }
        }
        JFinal.me().getServletContext().setAttribute("plugins", plugins);
        this.plugins = plugins;
    }

    @Override
    public void afterJFinalStart() {
        super.afterJFinalStart();
        systemProp.setProperty("zrlog.runtime.path", JFinal.me().getServletContext().getRealPath("/"));
        systemProp.setProperty("server.info", JFinal.me().getServletContext().getServerInfo());
        JFinal.me().getServletContext().setAttribute("system", systemProp);
        systemProperties.put("version", BlogBuildInfoUtil.getVersion());
        systemProperties.put("buildId", BlogBuildInfoUtil.getBuildId());
        systemProperties.put("buildTime", new SimpleDateFormat("yyyy-MM-dd").format(BlogBuildInfoUtil.getTime()));
        systemProperties.put("runMode", BlogBuildInfoUtil.getRunMode());
        JFinal.me().getServletContext().setAttribute("zrlog", systemProperties);
        JFinal.me().getServletContext().setAttribute("config", this);
        if (!isInstalled()) {
            LOGGER.warn("Not found lock file(" + PathKit.getWebRootPath() + "/WEB-INF/install.lock), Please visit the http://yourHostName:port" + JFinal.me().getContextPath() + "/install installation");
        } else {
            //检查数据文件是否需要更新
            String sqlVersion = WebSite.dao.getValueByName(com.zrlog.common.Constants.ZRLOG_SQL_VERSION_KEY);
            Integer updatedVersion = ZrlogUtil.doUpgrade(sqlVersion, PathKit.getWebRootPath() + "/WEB-INF/update-sql", dbProperties.getProperty("jdbcUrl"), dbProperties.getProperty("user"),
                    dbProperties.getProperty("password"), dbProperties.getProperty("driverClass"));
            if (updatedVersion > 0) {
                WebSite.dao.updateByKV(com.zrlog.common.Constants.ZRLOG_SQL_VERSION_KEY, updatedVersion + "");
            }
        }
    }

    @Override
    public void beforeJFinalStop() {
        PluginConfig.stopPluginCore();
    }

    @Override
    public void configInterceptor(Interceptors interceptors) {
        interceptors.add(new InitDataInterceptor());
        interceptors.add(new MyI18NInterceptor());
        interceptors.add(new BlackListInterceptor());
        interceptors.add(new RouterInterceptor());
    }

    @Override
    public void configHandler(Handlers handlers) {
        handlers.add(new PluginHandler());
        handlers.add(new StaticFileCheckHandler());
    }

    public void installFinish() {
        configPlugin(plugins);
        List<IPlugin> pluginList = plugins.getPluginList();
        for (IPlugin plugin : pluginList) {
            plugin.start();
        }
    }
}
