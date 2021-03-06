package com.pupilcc.pushbot.service;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.request.ParseMode;
import com.pengrad.telegrambot.request.SendMessage;
import com.pengrad.telegrambot.response.SendResponse;
import com.pupilcc.pushbot.entity.BotMessageDTO;
import com.pupilcc.pushbot.extension.ApiErrorCode;
import com.pupilcc.pushbot.extension.ApiResult;
import com.pupilcc.pushbot.users.Users;
import com.pupilcc.pushbot.users.UsersRepository;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

/**
 * 消息控制器
 * @author pupilcc
 */
@Service
public class MessageService {
    private final UsersRepository usersRepository;

    private final TelegramBot telegramBot;

    public MessageService(UsersRepository usersRepository, TelegramBot telegramBot) {
        this.usersRepository = usersRepository;
        this.telegramBot = telegramBot;
    }

    /**
     * 发送消息
     * @param text 消息内容
     * @param parseMode 消息格式
     * @param chatToken 用户Token
     * @return 响应信息
     */
    private boolean sendMessage(String text, ParseMode parseMode, String chatToken) {
        // 查找用户
        Users users = usersRepository.findByChatToken(chatToken);
        // 用户不存在
        if (ObjectUtils.isEmpty(users)) {
            return false;
        }

        // 给用户发送信息
        SendMessage sendMessage = new SendMessage(users.getChatId(), text);
        if (ObjectUtils.isNotEmpty(parseMode)) {
            sendMessage.parseMode(parseMode);
        }
        SendResponse sendResponse = telegramBot.execute(sendMessage);
        return sendResponse.isOk();
    }

    /**
     * 发送消息
     * @param dto 接收参数
     * @param chatToken 用户Token
     * @return 响应信息
     */
    public ApiResult<Object> sendMessage(BotMessageDTO dto, String chatToken) {
        ApiResult apiResult = checkParameter(dto, chatToken);
        if (!apiResult.ok()) {
            return apiResult;
        }

        boolean isSend = sendMessage(dto.getText(), dto.getParseMode(), chatToken);
        return ApiResult.success(isSend);
    }

    /**
     * 检查参数
     * @param dto 数据
     * @param chatToken 用户Token
     * @return 业务码
     */
    private ApiResult checkParameter(BotMessageDTO dto, String chatToken) {
        // 查找用户
        Users users = usersRepository.findByChatToken(chatToken);
        // 用户不存在
        if (ObjectUtils.isEmpty(users)) {
            return ApiResult.failed(ApiErrorCode.USER_NOT_EXIST);
        }

        // 参数检查
        if (ObjectUtils.isEmpty(dto) || StringUtils.isBlank(dto.getText())) {
            return ApiResult.failed(ApiErrorCode.TEXT_NULL);
        }

        return ApiResult.success();
    }
}
