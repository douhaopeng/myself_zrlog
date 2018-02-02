package com.zrlog.web.config;

import com.jfinal.config.Routes;

public class AdminRoutes extends Routes{
    @Override
    public void config() {
        add("/admin", AdminPageController.class);
        add("/admin/template", AdminTemplatePageController.class);
        add("/admin/article", AdminArticlePageController.class);
        add("/api/admin", AdminController.class);
        add("/api/admin/link", LinkController.class);
        add("/api/admin/comment", CommentController.class);
        add("/api/admin/tag", TagController.class);
        add("/api/admin/type", TypeController.class);
        add("/api/admin/nav", BlogNavController.class);
        add("/api/admin/article", ArticleController.class);
        add("/api/admin/website", WebSiteController.class);
        add("/api/admin/template", TemplateController.class);
        add("/api/admin/upload", UploadController.class);
        add("/api/admin/upgrade", UpgradeController.class);
    }
}
