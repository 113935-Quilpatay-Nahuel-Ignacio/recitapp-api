package com.recitapp.recitapp_api.modules.user.service;

import com.recitapp.recitapp_api.modules.user.dto.PurchaseHistoryDTO;

import java.util.List;

public interface PurchaseHistoryService {
    List<PurchaseHistoryDTO> getUserPurchaseHistory(Long userId);
}