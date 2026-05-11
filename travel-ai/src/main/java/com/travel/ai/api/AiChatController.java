package com.travel.ai.api;

import com.travel.common.api.ApiResult;
import com.travel.ai.application.AiApplicationService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * AI 对话与攻略 API（占位）。
 */
@RestController
@RequestMapping("/api/v1/ai")
public class AiChatController {

    private final AiApplicationService aiApplicationService;

    public AiChatController(AiApplicationService aiApplicationService) {
        this.aiApplicationService = aiApplicationService;
    }

    @PostMapping("/chat")
    public ApiResult<String> chat(@RequestBody ChatRequest request) {
        String reply = aiApplicationService.chat(request.sessionId(), request.message());
        return ApiResult.ok(reply);
    }

    @PostMapping("/guides")
    public ApiResult<String> guide(@RequestBody GuideRequest request) {
        String guide = aiApplicationService.guide(request.destination(), request.days(), request.budgetHint());
        return ApiResult.ok(guide);
    }

    public record ChatRequest(@NotBlank String sessionId, @NotBlank String message) {
    }

    public record GuideRequest(@NotBlank String destination, @Positive int days, String budgetHint) {
    }
}
