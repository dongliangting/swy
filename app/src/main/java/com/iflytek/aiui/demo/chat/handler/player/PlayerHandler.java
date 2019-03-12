package com.iflytek.aiui.demo.chat.handler.player;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.iflytek.aiui.demo.chat.handler.Answer;
import com.iflytek.aiui.demo.chat.handler.IntentHandler;
import com.iflytek.aiui.demo.chat.model.SemanticResult;
import com.iflytek.aiui.demo.chat.repository.player.AIUIPlayer;
import com.iflytek.aiui.demo.chat.ui.chat.ChatViewModel;
import com.iflytek.aiui.demo.chat.ui.chat.PlayerViewModel;
import com.iflytek.aiui.demo.chat.ui.common.PermissionChecker;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

/**
 * 播放器处理类，集成此类实现播放字段抽取方法
 * <p>
 * AIUI中音乐、故事、新闻等播放类的技能都包含如上一首，下一首的控制类语义，所以统一继承此类进行处理
 * <p>
 * 指令类语义结构类似， 一类intent属于Instruction，通过insType细分操作类型 prev next等，另一类直接通过intent区分
 */

public abstract class PlayerHandler extends IntentHandler {
    public PlayerHandler(ChatViewModel model, PlayerViewModel player, PermissionChecker checker) {
        super(model, player, checker);
    }

    /**
     * 首先将语义作为操控类意图处理，如果返回null说明不是定义内的操控类指令，之后按照内容播放类的意图
     * 处理。
     * @param result
     * @return
     */
    @Override
    public Answer getFormatContent(SemanticResult result) {
        Answer instructionAnswer = processAsInstruction(result);
        if (instructionAnswer != null) {
            return instructionAnswer;
        } else {
            return processAsContent(result);
        }
    }

    @Nullable
    private Answer processAsInstruction(SemanticResult result) {
        Answer formatAnswer = null;
        if (result.semantic != null) {
            String intent = result.semantic.optString("intent");
            if (!isTextContent(result)) {
                // 播放控制指令
                switch (intent.toUpperCase()) {
                    case "INSTRUCTION": {
                        formatAnswer = processInstructionIntent(result);
                    }
                    break;

                    case "SEARCH_NEXT":
                    case "NEXT": {
                        formatAnswer = next();
                    }
                    break;


                    case "LAST":
                    case "SEARCH_LAST":
                    case "PREVIOUS": {
                        formatAnswer = prev();
                    }
                    break;

                    case "PAUSE": {
                        formatAnswer = pausePlay();
                    }
                    break;

                    case "CONTINUE":
                    case "PLAY_CONTINUES": {
                        formatAnswer = resumePlay();
                    }
                    break;

                    case "NAME_QUERY": {
                        formatAnswer = broadcast();
                    }
                    break;

                    case "REPEAT":
                    case "REPLAY": {
                        formatAnswer = mMessageViewModel.getLatestAnswer();
                    }
                    break;

                    case "VOICE_UP": {
                        formatAnswer = volumeUp();
                    }
                    break;

                    case "VOICE_DOWN": {
                        formatAnswer = volumeDown();
                    }
                    break;
                }
            }
        }
        return formatAnswer;
    }

    /**
     * 处理信源列表播放类语义剩余的部分
     *
     * @param result
     * @return
     */
    private Answer processAsContent(SemanticResult result) {
        String answer = result.answer;
        List<AIUIPlayer.MediaInfo> songList = new ArrayList<>();
        if (result.data != null) {
            JSONArray list = result.data.optJSONArray("result");
            if (list != null) {
                for (int index = 0; index < list.length(); index++) {
                    JSONObject item = list.optJSONObject(index);
                    int contentType = item.optInt("type", -1);
                    switch (contentType) {
                        //文本内容
                        //无处理，应需要技能在处理时直接在Answer中拼接，参考目前文本笑话的处理
                        case 0: {
                            break;
                        }
                        // (-1) 无type字段，直接取字段构造Media内容
                        case -1: {
                            String url = extractURL(item);
                            String title = extractTitle(item);
                            String author = extractAuthor(item);
                            if (!TextUtils.isEmpty(url) && (url.contains(".mp3") || url.contains(".m4a"))) {
                                songList.add(new AIUIPlayer.MediaInfo(author, title, url));
                            }
                            break;
                        }
                        //音频内容(1) 视频内容(2)
                        case 1:
                        case 2: {
                            String url = extractURL(item);
                            String title = extractTitle(item);
                            String author = extractAuthor(item);
                            if (!TextUtils.isEmpty(url)) {
                                songList.add(new AIUIPlayer.MediaInfo(author, title, url));
                            }
                            break;
                        }
                    }
                }
            }

            if (retrieveCount() != -1 && retrieveCount() < songList.size()) {
                songList = songList.subList(0, retrieveCount());
            }

            if (songList.size() > 1 && isNeedShowControlTip()) {
                answer = result.answer + NEWLINE_NO_HTML + NEWLINE_NO_HTML + CONTROL_TIP;
            }
        }

        if (songList.size() != 0) {
            int startIndex = 0;
            for (int index = 0; index < songList.size(); index++) {
                AIUIPlayer.MediaInfo item = songList.get(index);
                String title = item.mediaName;
                if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(answer) && answer.contains(title)) {
                    startIndex = index;
                    break;
                }
            }

            mPlayer.playList(songList, startIndex);
            mPlayer.autoPause();
        }
        List<AIUIPlayer.MediaInfo> finalSongList = songList;
        return new Answer(answer, result.answer, () -> {
            if (finalSongList.size() != 0) {
                mPlayer.play();
            }
        });
    }

    /**
     * 处理指令类语义结果
     *
     * @param result
     * @return
     */
    @NonNull
    private Answer processInstructionIntent(SemanticResult result) {
        JSONArray slots = result.semantic.optJSONArray("slots");
        Answer answer = null;
        for (int index = 0; index < slots.length(); index++) {
            JSONObject slot = slots.optJSONObject(index);
            if ("insType".equals(slot.optString("name"))) {
                String instruction = slot.optString("value");
                switch (instruction.toLowerCase()) {
                    case "change":
                    case "change_name":
                    case "next_radio":
                    case "next": {
                        answer = next();
                    }
                    break;

                    case "previous":
                    case "past_radio":
                    case "past": {
                        answer = prev();
                    }
                    break;

                    case "pausePlay": {
                        answer = pausePlay();
                    }
                    break;

                    case "replay": {
                        answer = resumePlay();
                    }
                    break;

                    case "close": {
                        mPlayer.stop();
                        answer = new Answer("已为您停止");
                    }
                    break;

                    case "sleep": {
                        mPlayer.stop();
                        answer = new Answer("那我走了，记得常来找我");
                    }
                    break;

                    case "broadcast":
                    case "broadcastsinger": {
                        answer = broadcast();
                    }
                    break;

                    //replayAnswer小写
                    case "replayanswer": {
                        answer = mMessageViewModel.getLatestAnswer();
                    }
                    break;

                    case "volume_plus": {
                        answer = volumeUp();
                    }
                    break;

                    case "volume_minus": {
                        answer = volumeDown();
                    }
                    break;

                    case "volume_max": {
                        mPlayer.volumePercent(1);
                        answer = new Answer("已为您将音量调到最大", () -> mPlayer.resumeIfNeed());
                    }
                    break;

                    case "volume_min": {
                        mPlayer.volumePercent(0);
                        answer = new Answer("已为您将音量调到最小", () -> mPlayer.resumeIfNeed());
                    }
                    break;

                    case "volume_mid": {
                        mPlayer.volumePercent(0.5f);
                        answer = new Answer("已为您将音量调到中等", () -> mPlayer.resumeIfNeed());
                    }
                    break;

                    case "volume_select": {
                        String series = optValue(slots, "series");
                        try {
                            int volume = Integer.parseInt(series);
                            mPlayer.volumePercent(Math.min(volume, 100) / 100.0f);
                            answer = new Answer("已为您将音量调到" + volume, () -> mPlayer.resumeIfNeed());
                        } catch (NumberFormatException e) {
                            Timber.e("volume select error %s", series);
                        }
                    }
                    break;
                }
            }
        }

        return answer;
    }

    @NonNull
    private Answer volumeDown() {
        Answer answer;
        mPlayer.volumeMinus();
        answer = new Answer("已为您调低音量", () -> mPlayer.resumeIfNeed());
        return answer;
    }

    @NonNull
    private Answer volumeUp() {
        Answer answer;
        mPlayer.volumePlus();
        answer = new Answer("已为您调高音量", () -> mPlayer.resumeIfNeed());
        return answer;
    }


    private Answer resumePlay() {
        AIUIPlayer.MediaInfo current = mPlayer.current();
        Answer formatAnswer = null;
        if (current != null) {
            formatAnswer = new Answer(infoTips("为您继续播放", current), () -> mPlayer.play());
        }

        return formatAnswer;
    }

    private Answer pausePlay() {
        mPlayer.manualPause();
        return new Answer("已为您暂停");
    }

    private Answer prev() {
        AIUIPlayer.MediaInfo prev = mPlayer.prev();
        String answer;
        if (prev == null) {
            answer = "当前已经是播放列表的第一项";
        } else {
            answer = infoTips("即将为您播放", prev);
        }

        mPlayer.autoPause();
        return new Answer(answer, () -> mPlayer.play());
    }

    private Answer next() {
        AIUIPlayer.MediaInfo next = mPlayer.next();
        String answer;
        if (next == null) {
            answer = "当前已经是播放列表的最后一项";
        } else {
            answer = infoTips("即将为您播放", next);
        }


        mPlayer.autoPause();
        return new Answer(answer, () -> mPlayer.play());
    }

    @NonNull
    private Answer broadcast() {
        Answer formatAnswer;
        AIUIPlayer.MediaInfo current = mPlayer.current();
        if (current != null) {
            formatAnswer = new Answer(infoTips("现在正在播放的是", current), () -> mPlayer.resumeIfNeed());
        } else {
            formatAnswer = new Answer("当前没有播放");
        }
        return formatAnswer;
    }

    private String infoTips(String prefix, AIUIPlayer.MediaInfo media) {
        String answer = null;
        if (media != null) {
            String author = TextUtils.isEmpty(media.author) ? "" : media.author + "的";
            answer = prefix + author + media.mediaName;
        }
        return answer;
    }


    /**
     * 信源内容是否是文本内容（文本新闻，文本笑话等）
     *
     * @param result
     * @return
     */
    private boolean isTextContent(SemanticResult result) {
        if (result.data != null) {
            JSONArray list = result.data.optJSONArray("result");
            if (list != null && list.length() != 0) {
                for (int index = 0; index < list.length(); index++) {
                    JSONObject item = list.optJSONObject(index);
                    int contentType = item.optInt("type", -1);
                    switch (contentType) {
                        // (-1) 无type字段，直接取字段构造Media内容
                        case -1: {
                            String url = extractURL(item);
                            if (!TextUtils.isEmpty(url) && (url.contains(".mp3") || url.contains(".m4a"))) {
                                return false;
                            }
                            break;
                        }
                        //音频内容(1) 视频内容(2)
                        case 1:
                        case 2: {
                            return false;
                        }
                    }
                }
                return true;
            }
        }

        return false;
    }

    private String optValue(JSONArray slots, String key) {
        for (int index = 0; index < slots.length(); index++) {
            JSONObject slot = slots.optJSONObject(index);
            if (key.equals(slot.optString("name"))) {
                return slot.optString("value");
            }
        }

        return null;
    }

    /**
     * 列表中需要播放的项目数
     *
     * @return -1 整个列表
     */
    protected int retrieveCount() {
        return -1;
    }

    protected abstract String extractURL(JSONObject item);

    protected abstract String extractTitle(JSONObject item);

    protected abstract String extractAuthor(JSONObject item);
}

