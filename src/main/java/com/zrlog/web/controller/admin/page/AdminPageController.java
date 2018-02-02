package com.zrlog.web.controller.admin.page;

import com.jfinal.core.JFinal;
import com.zrlog.model.Comment;
import com.zrlog.model.Log;
import com.zrlog.util.ParseUtil;
import com.zrlog.web.controller.BaseController;
import com.zrlog.web.controller.admin.api.UpgradeController;
import com.zrlog.web.token.AdminTokenService;
import com.zrlog.web.token.AdminTokenThreadLocal;

import java.util.List;
import java.util.Map;

public class AdminPageController extends BaseController{
    private AdminTokenService adminTokenService = new AdminTokenService();
    public String index() {
        if (AdminTokenThreadLocal.getUser() != null) {
            Map<String, Object> commentMap = Comment.dao.noRead(1, 5);
            if (commentMap.get("rows") != null) {
                List<Comment> rows = (List<Comment>) commentMap.get("rows");
                for (Comment comment : rows) {
                    comment.put("userComment", ParseUtil.autoDigest(comment.get("userComment").toString(), 15));
                }
            }
            JFinal.me().getServletContext().setAttribute("comments", commentMap);
            JFinal.me().getServletContext().setAttribute("commCount", Comment.dao.getCommentCount());
            JFinal.me().getServletContext().setAttribute("toDayCommCount", Comment.dao.getToDayCommentCount());
            JFinal.me().getServletContext().setAttribute("clickCount", Log.dao.sumAllClick());
            JFinal.me().getServletContext().setAttribute("lastVersion", new UpgradeController().lastVersion());
            if (getPara(0) == null || getRequest().getRequestURI().endsWith("admin/") || "login".equals(getPara(0))) {
                redirect("/admin/index");
                return null;
            } else {
                return "/admin/" + getPara(0);
            }
        } else {
            return "/admin/login";
        }
    }

    public String login() {
        if (AdminTokenThreadLocal.getUser() != null) {
            redirect("/admin/index");
            return null;
        } else {
            return "/admin/login";
        }
    }
}
