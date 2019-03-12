package com.iflytek.aiui.demo.chat.handler.player;

import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;

import org.json.JSONObject;

/**
 * 广播技能处理类
 */

public class RadioDisposer extends PlayerHandler {
    public RadioDisposer(ChatViewModel model, PlayerViewModel player, PermissionChecker checker) {
        super(model, player, checker);
    }

    @Override
    protected String extractURL(JSONObject item) {
        return item.optString("url");
    }

    @Override
    protected String extractTitle(JSONObject item) {
        return item.optString("name");
    }

    @Override
    protected String extractAuthor(JSONObject item) {
        return "";
    }
}

