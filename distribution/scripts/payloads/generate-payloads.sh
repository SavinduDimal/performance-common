#!/bin/bash -e
# Copyright 2017 WSO2 Inc. (http://wso2.org)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# ----------------------------------------------------------------------------
# Generate JSON Payloads
# ----------------------------------------------------------------------------

script_dir=$(dirname "$0")
payload_type=""
declare -a payloads
ai_api_payload=false
ai_text_pattern="the quick brown fox jumps over the lazy dog "

function generate_ai_text() {
    local target_size="$1"
    local text=""

    while [[ ${#text} -lt $target_size ]]; do
        text+="$ai_text_pattern"
    done

    echo "${text:0:$target_size}"
}

function usage() {
    echo ""
    echo "Usage: "
    echo "$0 [-p <payload_type>] [-s <payload_size>] [-a]"
    echo ""
    echo "-p: The Payload Type."
    echo "-s: The Payload Size. You can give multiple payload sizes."
    echo "-a: Generate AI API chat completion request payloads."
    echo "-h: Display this help and exit."
    echo ""
}

while getopts "p:s:ah" opts; do
    case $opts in
    p)
        payload_type=${OPTARG}
        ;;
    s)
        payloads+=("${OPTARG}")
        ;;
    a)
        ai_api_payload=true
        ;;
    h)
        usage
        exit 0
        ;;
    \?)
        usage
        exit 1
        ;;
    esac
done

if [[ -z $payload_type ]]; then
    payload_type="simple"
fi

if [[ ${#payloads[@]} -eq 0 ]]; then
    payloads=("50 1024 10240 102400")
fi

for s in ${payloads[*]}; do
    if [ "$ai_api_payload" = true ]; then
        echo "Generating ai_${s}B.json file"
        json_prefix='{"model":"mistral-small-latest","temperature":0.7,"top_p":1,"max_tokens":300,"min_tokens":0,"stream":false,"stop":"string","random_seed":0,"messages":[{"role":"user","content":"'
        json_suffix='"}],"response_format":{"type":"text"},"safe_prompt":false}'
        prompt_size=$(("$s" - ${#json_prefix} - ${#json_suffix} - 1))
        prompt=$(generate_ai_text "$prompt_size")
        printf '{"model":"mistral-small-latest","temperature":0.7,"top_p":1,"max_tokens":300,"min_tokens":0,"stream":false,"stop":"string","random_seed":0,"messages":[{"role":"user","content":"%s"}],"response_format":{"type":"text"},"safe_prompt":false}\n' "$prompt" >"ai_${s}B.json"
    else
        echo "Generating ${s}B file"
        java -jar $script_dir/payload-generator-${performance.common.version}.jar --size $s --payload-type ${payload_type}
    fi
done
