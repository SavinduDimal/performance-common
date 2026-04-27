/*
 * Copyright 2026 WSO2 Inc. (http://wso2.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.performance.common.netty.echo;

import java.nio.charset.StandardCharsets;

/**
 * Fixed-size AI API mock response used by APIM AI API performance tests.
 */
final class AiChatCompletionResponse {

    private static final String TEXT_PATTERN = "the quick brown fox jumps over the lazy dog ";

    private AiChatCompletionResponse() {
    }

    static String createContent(int responseSizeBytes) {
        String prefix = "{\"id\":\"cmpl-mock-perf-test-00000000\",\"object\":\"chat.completion\","
                + "\"created\":1713225600,\"model\":\"mistral-small-latest\",\"choices\":[{\"index\":0,"
                + "\"message\":{\"role\":\"assistant\",\"content\":\"";
        String suffix = "\"},\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":15,"
                + "\"completion_tokens\":200,\"total_tokens\":215}}";
        int paddingLength = responseSizeBytes - byteLength(prefix) - byteLength(suffix);
        if (paddingLength < 0) {
            throw new IllegalStateException("AI chat completion response template exceeds configured size");
        }
        StringBuilder content = new StringBuilder(responseSizeBytes);
        content.append(prefix);
        appendText(content, paddingLength);
        content.append(suffix);
        return content.toString();
    }

    private static void appendText(StringBuilder content, int length) {
        while (length > 0) {
            int chunkLength = Math.min(length, TEXT_PATTERN.length());
            content.append(TEXT_PATTERN, 0, chunkLength);
            length -= chunkLength;
        }
    }

    private static int byteLength(String value) {
        return value.getBytes(StandardCharsets.UTF_8).length;
    }
}
