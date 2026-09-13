package dev.docmind.dto;

import java.util.List;

public class AskResponse {

    private final String answer;
    private final List<SearchResultResponse> sources;

    public AskResponse(
            String answer,
            List<SearchResultResponse> sources) {

        this.answer = answer;
        this.sources = sources;
    }

    public String getAnswer() {
        return answer;
    }

    public List<SearchResultResponse> getSources() {
        return sources;
    }
}