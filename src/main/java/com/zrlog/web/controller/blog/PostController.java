package com.zrlog.web.controller.blog;

import com.zrlog.web.controller.BaseController;

import java.util.Map;

public class PostController extends BaseController{
    private void setPageInfo(String currentUri, Map<String, Object> data, int currentPage){
        setAttr("yurl",currentUri);
        Integer total = (Integer) data.get("total");
    }
}
