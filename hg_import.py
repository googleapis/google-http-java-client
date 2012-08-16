#!/usr/bin/python2.6
#
# Copyright (c) 2012 Google Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
# in compliance with the License. You may obtain a copy of the License at
#
# http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software distributed under the License
# is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
# or implied. See the License for the specific language governing permissions and limitations under
# the License.

"""Utility method to import a diff from codereview.appspot.com and also apply hg renames."""

import urllib
import subprocess
import re
import sys

fp = re.compile('^rename from (\S+)$')
tp = re.compile('^rename to (\S+)$')
hg_cmd = 'hg'

if len(sys.argv) == 1:
  print 'missing codereview.appspot.com URL'
  sys.exit(1)

url = sys.argv[1]

subprocess.check_call([hg_cmd, 'import', '--no-commit', url])
webFile = urllib.urlopen(url)
for line in webFile.readlines():
  m = fp.search(line)
  if m:
    hg_from = m.group(1)
  m = tp.search(line)
  if m:
    hg_to = m.group(1)
    subprocess.check_call([hg_cmd, 'mv', hg_from, hg_to])
webFile.close()

