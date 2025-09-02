package com.sweta.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LiveStatsDTO {
    private int activeViewers;
    private int countries;
    private int profileViewsThisMonth;
}