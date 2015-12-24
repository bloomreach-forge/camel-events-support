/*
 *  Copyright 2012-2013 Hippo B.V. (http://www.onehippo.com)
 * 
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * This script invokes SyntaxHighlighter.highlight() functions
 * for all the source elements in maven site documentation.
 * Because the default SyntaxHighlighter.all() function finds elements by tagName (e.g., 'pre') only,
 * this script finds all the generated source elements by selecting the elements by the proper selector
 * and invokes SyntaxHighlighter.highlight() function for each selected element.
 * 
 * Note: this function depends on SyntaxHighlighter libraries and jquery core library.
 */
$(document).ready(function() {
  var brushRe = /^brush:/;
  $('div.source > pre').each(function(i, elem) {
    var styleClass = $(elem).attr("class");
    if (styleClass && brushRe.test(styleClass)) {
      SyntaxHighlighter.highlight(null, elem);
    } else {
      styleClass = $(elem).parent().attr("class");
      // when there's a user-added parent wrapper element containing brush class
      if (!styleClass || !brushRe.test(styleClass)) {
        styleClass = $(elem).parent().parent().attr("class");
      }
      if (styleClass && styleClass.match(/^brush:/)) {
        $(elem).addClass(styleClass);
        SyntaxHighlighter.highlight(null, elem);
      }
    }
  });
});
