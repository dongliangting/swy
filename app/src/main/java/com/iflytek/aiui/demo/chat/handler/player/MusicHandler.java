package com.iflytek.aiui.demo.chat.handler.player;

import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;

import org.json.JSONObject;

/**
 * 音乐技能和操作指令处理处理
 */

public class MusicHandler extends PlayerHandler {
    public MusicHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker) {
        super(model, player, checker);
    }

    @Override
    protected String extractURL(JSONObject item) {
        return item.optString("audiopath");
    }

    @Override
    protected String extractTitle(JSONObject item) {
        return item.optString("songname");
    }

    @Override
    protected String extractAuthor(JSONObject item) {
        return item.optJSONArray("singernames").optString(0);
    }
}

