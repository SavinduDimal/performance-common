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

    static final String CONTENT = createContent();

    private static final int RESPONSE_SIZE_BYTES = 1024;

    private AiChatCompletionResponse() {
    }

    private static String createContent() {
        String prefix = "{\"id\":\"cmpl-mock-perf-test-00000000\",\"object\":\"chat.completion\","
                + "\"created\":1713225600,\"model\":\"mistral-small-latest\",\"choices\":[{\"index\":0,"
                + "\"message\":{\"role\":\"assistant\",\"content\":\"";
        String suffix = "\"},\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":15,"
                + "\"completion_tokens\":200,\"total_tokens\":215}}";
        int paddingLength = RESPONSE_SIZE_BYTES - byteLength(prefix) - byteLength(suffix);
        if (paddingLength < 0) {
            throw new IllegalStateException("AI chat completion response template exceeds 1KB");
        }
        StringBuilder content = new StringBuilder(RESPONSE_SIZE_BYTES);
        content.append(prefix);
        for (int i = 0; i < paddingLength; i++) {
            content.append('x');
        }
        content.append(suffix);
        return content.toString();
    }

    private static int byteLength(String value) {
        return value.getBytes(StandardCharsets.UTF_8).length;
    }
}
