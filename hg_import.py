#!/usr/bin/python
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
import os.path

file_name_re = re.compile('^Index: (\S+)$')
deleted_re = re.compile('^deleted file mode \S+$')
fp = re.compile('^rename from (\S+)$')
tp = re.compile('^rename to (\S+)$')
fcp = re.compile('^copy from (\S+)$')
tcp = re.compile('^copy to (\S+)$')
hg_cmd = 'hg'

if len(sys.argv) == 1:
  print 'missing codereview.appspot.com URL'
  sys.exit(1)

if not os.path.exists('.hg'):
  print 'must be run from the root directory of the hg workspace'
  sys.exit(1)

if '-f' == sys.argv[1]:
  i = 2
  force = ['-f']
else:
  i = 1
  force = []
url = sys.argv[i]

subprocess.check_call([hg_cmd, 'import'] + force + ['--no-commit', url])
webFile = urllib.urlopen(url)
for line in webFile.readlines():
  # detect file name
  m = file_name_re.search(line)
  if m:
    file_name = m.group(1)
    print '___ %s' % file_name
  # detect deleted file
  m = deleted_re.search(line)
  if m:
    subprocess.check_call([hg_cmd, 'rm', '-f', file_name])
  # detect moved file
  m = fp.search(line)
  if m:
    hg_from = m.group(1)
  m = tp.search(line)
  if m:
    hg_to = m.group(1)
    subprocess.check_call([hg_cmd, 'mv', '-f', hg_from, hg_to])
  # detect copied file
  m = fcp.search(line)
  if m:
    hg_from = m.group(1)
  m = tcp.search(line)
  if m:
    hg_to = m.group(1)
    subprocess.check_call([hg_cmd, 'cp', hg_from, hg_to])
    subprocess.check_call([hg_cmd, 'revert', '--no-backup', hg_from])
webFile.close()

