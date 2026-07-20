package com.xhzb.nursing.service;

import java.util.List;

public interface ChatHistoryService {
    void saveChatHistory(String userId,String chatId);

    List<String> getChatHistory(String userId);

    void deleteHistory(String chatId);
}
