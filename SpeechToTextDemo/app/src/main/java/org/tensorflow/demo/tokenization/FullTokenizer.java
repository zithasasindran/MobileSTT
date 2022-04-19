/* Copyright 2019 The TensorFlow Authors. All Rights Reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
==============================================================================*/
package org.tensorflow.demo.tokenization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A java realization of Bert tokenization. Original python code:
 * https://github.com/google-research/bert/blob/master/tokenization.py runs full tokenization to
 * tokenize a String into split subtokens or ids.
 */
public final class FullTokenizer {
  private static BasicTokenizer basicTokenizer;
  private static WordpieceTokenizer wordpieceTokenizer;
  private static Map<String, Integer> dic;

  public FullTokenizer(Map<String, Integer> inputDic, boolean doLowerCase) {
    dic = inputDic;
    basicTokenizer = new BasicTokenizer(doLowerCase);
    wordpieceTokenizer = new WordpieceTokenizer(inputDic);
  }

  public static List<String> tokenize(String text) {
    List<String> splitTokens = new ArrayList<>();
    for (String token : basicTokenizer.tokenize(text)) {
      splitTokens.addAll(wordpieceTokenizer.tokenize(token));
    }
    return splitTokens;
  }

  public static List<Integer> convertTokensToIds(List<String> tokens) {
    List<Integer> outputIds = new ArrayList<>();
    for (String token : tokens) {
      outputIds.add(dic.get(token));
    }
    return outputIds;
  }
}
