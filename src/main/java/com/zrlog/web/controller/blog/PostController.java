package com.zrlog.web.controller.blog;

import com.zrlog.util.I18NUtil;
import com.zrlog.web.controller.BaseController;
import com.zrlog.web.util.WebTools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostController extends BaseController{
    private void setPageInfo(String currentUri, Map<String, Object> data, int currentPage){
        setAttr("yurl",currentUri);
        Integer total = (Integer) data.get("total");
        if(total != null){
            setAttr("data",data);
            if(total>1){
                fullPager(currentUri,currentPage,total);
            }
        }
    }

    private void fullPager(String currentUri, int currentPage, Integer total) {
        Map<String, Object> pager = new HashMap<>();
        List<Map<String, Object>> pageList = new ArrayList<>();
        if (currentPage != 1) {
            pageList.add(pageEntity(currentUri, currentPage, I18NUtil.getStringFromRes("prevPage", getRequest()), currentPage - 1));
        }
        if (total > 10) {
            if (currentPage < 3 || total - 4 < currentPage) {
                for (int i = 1; i <= 4; i++) {
                    pageList.add(pageEntity(currentUri, currentPage, i));
                }
            } else {
                if (currentPage + 1 == total - 3) {
                    pageList.add(pageEntity(currentUri, currentPage, currentPage - 3));
                }
                for (int i = currentPage - 2; i <= currentPage; i++) {
                    pageList.add(pageEntity(currentUri, currentPage, i));
                }
                if (currentPage + 1 != total - 3) {
                    pageList.add(pageEntity(currentUri, currentPage, currentPage + 1));
                }
            }
            for (int i = total - 3; i <= total; i++) {
                pageList.add(pageEntity(currentUri, currentPage, i));
            }
        } else {
            for (int i = 1; i <= total; i++) {
                pageList.add(pageEntity(currentUri, currentPage, i));
            }
        }
        if (currentPage != total) {
            pageList.add(pageEntity(currentUri, currentPage, I18NUtil.getStringFromRes("nextPage", getRequest()), currentPage + 1));
        }
        pager.put("pageList", pageList);
        pager.put("pageStartUrl", currentUri + 1);
        pager.put("pageEndUrl", currentUri + total);
        pager.put("startPage", currentPage == 1);
        pager.put("endPage", currentPage == total);
        setAttr("pager", pager);
    }
    private Map<String, Object> pageEntity(String url, int currentPage, String desc, int page) {
        Map<String, Object> map = new HashMap<>();
        map.put("url", url + page);
        map.put("desc", desc);
        map.put("current", currentPage == page);
        return map;
    }

    private Map<String, Object> pageEntity(String url, int currentPage, int page) {
        return pageEntity(url, currentPage, page + "", page);
    }

    public String index() {
        if ((getRequest().getServletPath().startsWith("/post"))
                && (getPara(0) != null)) {
            if (getPara(0).equals("all")) {
                return all();
            } else if (getPara(0) != null) {
                return detail();
            } else {
                return all();
            }
        } else {
            return all();
        }
    }

    public String search() {
        String key;
        Map<String, Object> data;
        if (getParaToInt(1) == null) {
            if (isNotNullOrNotEmptyStr(getPara("key"))) {
                if ("GET".equals(getRequest().getMethod())) {
                    key = convertRequestParam(getPara("key"));
                } else {
                    key = getPara("key");
                }
                data = articleService.searchArticle(1, getDefaultRows(), key);
            } else {
                return all();
            }

        } else {
            key = convertRequestParam(getPara(0));
            data = articleService.searchArticle(getParaToInt(1), getDefaultRows(), key);
        }
        // 记录回话的Key
        setAttr("key", WebTools.htmlEncode(key));

        setAttr("tipsType", I18NUtil.getStringFromRes("search", getRequest()));
        setAttr("tipsName",  WebTools.htmlEncode(key));

        setPageInfo("post/search/" + key + "-", data, getParaToInt(1, 1));
        return "page";
    }
}
