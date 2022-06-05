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

    public List<WikiBotConfig> getConfigs() { // Lombok-generated methods do not work in Kotlin in IntelliJ IDEA
        return configs;
    }
}
