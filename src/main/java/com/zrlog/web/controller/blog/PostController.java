package com.zrlog.web.controller.blog;

import com.zrlog.util.I18NUtil;
import com.zrlog.web.controller.BaseController;

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
}
