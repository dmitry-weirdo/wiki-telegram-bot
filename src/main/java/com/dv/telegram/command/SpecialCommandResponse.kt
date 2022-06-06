package com.dv.telegram.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

@Data
@AllArgsConstructor
public class SpecialCommandResponse {
    public final Optional<String> response;
    public final boolean useMarkdownInResponse;

    public boolean hasResponse() {
        return response.isPresent();
    }

    public static SpecialCommandResponse noResponse() {
        return new SpecialCommandResponse(Optional.empty(), false);
    }

    public static SpecialCommandResponse withResponse(String response, boolean useMarkdownInResponse) {
        if (StringUtils.isBlank(response)) {
            throw new IllegalArgumentException("response cannot be blank.");
        }

        return new SpecialCommandResponse(Optional.of(response), useMarkdownInResponse);
    }
}
