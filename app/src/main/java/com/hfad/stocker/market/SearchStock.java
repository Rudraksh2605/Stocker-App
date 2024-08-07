package com.hfad.stocker.market;

import com.hfad.stocker.api.ApiResponseItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchStock {

    public List<ApiResponseItem> searchStockByName(Map<String, ApiResponseItem> stockMap, String companyName) {
        List<ApiResponseItem> filteredList = new ArrayList<>();
        for (Map.Entry<String, ApiResponseItem> entry : stockMap.entrySet()) {
            if (entry.getKey().contains(companyName.toLowerCase())) {
                filteredList.add(entry.getValue());
            }
        }
        return filteredList;
    }
}

