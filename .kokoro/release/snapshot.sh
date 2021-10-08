#!/bin/bash
# Copyright 2019 Google LLC
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -eo pipefail

source $(dirname "$0")/common.sh
MAVEN_SETTINGS_FILE=$(realpath $(dirname "$0")/../../)/settings.xml
pushd $(dirname "$0")/../../

# ensure we're trying to push a snapshot (no-result returns non-zero exit code)
grep SNAPSHOT versions.txt

setup_environment_secrets
create_settings_xml_file "settings.xml"

mvn clean deploy -B \
  --settings ${MAVEN_SETTINGS_FILE} \
  -DperformRelease=true \
  -Dgpg.executable=gpg \
  -Dgpg.passphrase=${GPG_PASSPHRASE} \
  -Dgpg.homedir=${GPG_HOMEDIR}
