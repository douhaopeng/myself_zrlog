package com.zrlog.model;

import com.jfinal.plugin.activerecord.Model;

public class Comment extends Model<Comment>{
    public static final Comment dao = new Comment();
    private static final long serialVersionUID = 1L;
}
