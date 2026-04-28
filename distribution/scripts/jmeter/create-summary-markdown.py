#!/usr/bin/env python
# -*- coding: utf-8 -*-
# Copyright (c) 2018, WSO2 Inc. (http://wso2.org) All Rights Reserved.
#
# WSO2 Inc. licenses this file to you under the Apache License,
# Version 2.0 (the "License"); you may not use this file except
# in compliance with the License.
# You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.
#
# ----------------------------------------------------------------------------
# Create performance test results summary markdown file
# ----------------------------------------------------------------------------

import argparse
import csv
import json
import os
import re
import humanize
from jinja2 import Environment, FileSystemLoader

PATH = os.path.dirname(os.path.abspath(__file__))
TEMPLATE_ENVIRONMENT = Environment(
    autoescape=False,
    loader=FileSystemLoader(os.path.join(PATH, 'templates')),
    trim_blocks=False,
    keep_trailing_newline=True)


def render_template(template_filename, context):
    return TEMPLATE_ENVIRONMENT.get_template(template_filename).render(context)


class StoreJsonParameters(argparse.Action):
    def __call__(self, parser, namespace, values, option_string=None):
        my_dict = {}
        for kv in values.split(","):
            k, v = kv.split("=")
            with open(v) as f:
                if k in my_dict:
                    my_dict[k].update(json.load(f))
                else:
                    my_dict[k] = json.load(f)
        setattr(namespace, self.dest, my_dict)


def pluralize(number, singular='', plural='s'):
    if int(number) == 1:
        return singular
    else:
        return plural


def numeric_key(value):
    if value is None or value == 'N/A':
        return float('inf')
    try:
        return float(value)
    except ValueError:
        match = re.match(r'^(\d+(?:\.\d+)?)([A-Za-z]+)$', value)
        if match:
            return (float(match.group(1)), match.group(2))
        return value


def main():
    parser = argparse.ArgumentParser(description='Create summary report')
    parser.add_argument("--json-parameters", dest="parameters", action=StoreJsonParameters,
                        help="Additional parameters in JSON files with a prefix.", metavar="prefix1=json-file1,prefix2=json-file2...")
    parser.add_argument('--column-names', required=True,
                        help='Columns to include in the report.', nargs='+', type=str)
    args = parser.parse_args()

    context = {
        'column_names': args.column_names
    }

    if args.parameters is not None:
        context.update(args.parameters)

    column_names_set = set(args.column_names)
    rows = []
    test_scenarios = context.get('parameters', {}).get('test_scenarios', [])
    scenario_order = {
        scenario.get('display_name'): index for index, scenario in enumerate(test_scenarios)
    }

    with open('summary.csv') as csv_file:
        reader = csv.DictReader(csv_file)
        for row in reader:
            filtered_row = dict(
                (name, value) for name, value in row.items() if name in column_names_set
            )
            rows.append(filtered_row)
        sort_columns = [
            'Heap Size',
            'Concurrent Users',
            'Message Size (Bytes)',
            'Response Size (Bytes)',
            'Back-end Service Delay (ms)'
        ]
        if all(column in column_names_set for column in sort_columns) and 'Scenario Name' in column_names_set:
            rows.sort(
                key=lambda row: (
                    numeric_key(row.get('Heap Size')),
                    numeric_key(row.get('Concurrent Users')),
                    numeric_key(row.get('Message Size (Bytes)')),
                    numeric_key(row.get('Response Size (Bytes)')),
                    numeric_key(row.get('Back-end Service Delay (ms)')),
                    scenario_order.get(row.get('Scenario Name'), len(scenario_order)),
                )
            )
        context['rows'] = rows

    TEMPLATE_ENVIRONMENT.filters['pluralize'] = pluralize
    TEMPLATE_ENVIRONMENT.filters['humanize_ordinal'] = humanize.ordinal
    markdown_filename = "summary.md"
    with open(markdown_filename, 'wb') as f:
        markdown_file = render_template(markdown_filename, context)
        f.write(markdown_file.encode("utf-8"))


if __name__ == "__main__":
    main()
