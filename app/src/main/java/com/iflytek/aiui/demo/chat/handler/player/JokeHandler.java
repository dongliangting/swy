package com.iflytek.aiui.demo.chat.handler.player;

import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;

import org.json.JSONObject;

/**
 * 笑话播放处理类
 */

public class JokeHandler extends PlayerHandler {
    public JokeHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker) {
        super(model, player, checker);
    }

    @Override
    protected String extractURL(JSONObject item) {
        return item.optString("mp3Url");
    }

    @Override
    protected String extractTitle(JSONObject item) {
        return item.optString("title");
    }

    @Override
    protected String extractAuthor(JSONObject item) {
        return "";
    }
}

