/**
 * Copyright 2013 Miere Liniel Teixeira
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
package org.layr.engine.components;

import java.io.IOException;
import java.io.Writer;

import org.layr.commons.StringUtil;


public class TextNode extends GenericComponent {

	public TextNode(String content) {
		this.setTextContent(content);
	}

	@Override
	public void configure() {}

	@Override
	public void render() throws IOException {
		Writer writer = requestContext.getWriter();
		String value = getTextContent();
		if (!StringUtil.isEmpty(value))
			writer.append(value);
	}

}
