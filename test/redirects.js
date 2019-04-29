// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

'use strict';

const assert = require('assert');
const redirectPath = require('../src/redirects.js').redirectPath;

describe('redirectUrl', () => {

  it('finds a versioned doc', () => {
    const path = 'google-http-java-client/releases/1.25.0/javadoc/index.html';
    const expected = 'java/google-http-client/1.25.0/index.html';
    assert.equal(redirectPath(path), expected);
  });

  it('finds a deeplink', () => {
    const path = 'google-http-java-client/releases/1.25.0/javadoc/index.html?com/google/api/client/http/HttpIOExceptionHandler.html';
    const expected = 'java/google-http-client/1.25.0/index.html?com/google/api/client/http/HttpIOExceptionHandler.html';
    assert.equal(redirectPath(path), expected);
  });

  it('defaults to the latest docs', () => {
    const path = 'abcd';
    const expected = null;
    assert.equal(redirectPath(path), expected);
  });
});