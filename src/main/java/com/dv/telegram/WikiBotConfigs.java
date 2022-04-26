package com.dv.telegram;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class WikiBotConfigs {
    public List<WikiBotConfig> configs = new ArrayList<>();

    public void fillDefaults() {
        configs.forEach(WikiBotConfig::fillDefaults);
    }
}
