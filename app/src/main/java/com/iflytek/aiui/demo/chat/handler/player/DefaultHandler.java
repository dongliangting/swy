package com.iflytek.aiui.demo.chat.handler.player;

import android.text.TextUtils;

import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;

import org.json.JSONObject;

/**
 *
 * 默认处理类，播报answer，播放data中符合格式的可播放内容
 *
 */

public class DefaultHandler extends PlayerHandler {
    public DefaultHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker) {
        super(model, player, checker);
    }

    @Override
    protected String extractURL(JSONObject item) {
        return item.optString("url");
    }

    @Override
    protected String extractTitle(JSONObject item) {
        String title = item.optString("title");
        if(TextUtils.isEmpty(title)) {
            title = item.optString("name");
        }
        return title;
    }

    @Override
    protected String extractAuthor(JSONObject item) {
        return "";
    }
}
