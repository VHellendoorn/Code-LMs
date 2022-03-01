/*******************************************************************************
 * Copyright (c) 2011 Tran Nam Quang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tran Nam Quang - initial API and implementation
 *******************************************************************************/

package net.sourceforge.docfetcher.model.parse;

import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;

import net.sourceforge.docfetcher.enums.Msg;

import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.rtf.TextExtractor;
import org.apache.tika.sax.BodyContentHandler;
import org.apache.tika.sax.XHTMLContentHandler;

/**
 * @author Tran Nam Quang
 */
final class RtfParser extends StreamParser {
	
	private static final Collection<String> extensions = Collections.singleton("rtf");
	private static final Collection<String> types = MediaType.Col.text("rtf");

	protected ParseResult parse(InputStream in, ParseContext context)
			throws ParseException {
		BodyContentHandler bodyHandler = new BodyContentHandler(-1);
		Metadata metadata = new Metadata();
		XHTMLContentHandler handler = new XHTMLContentHandler(bodyHandler, metadata);
		TextExtractor extractor = new TextExtractor(handler, metadata);
		try {
			extractor.extract(in);
			
			/*
			 * See TextExtractor#processControlWord() for a list of the
			 * available metadata.
			 */
			return new ParseResult(bodyHandler.toString())
				.addAuthor(metadata.get(Metadata.AUTHOR))
				.setTitle(metadata.get(Metadata.TITLE))
				.addMiscMetadata(metadata.get(Metadata.SUBJECT))
				.addMiscMetadata(metadata.get(Metadata.KEYWORDS))
				.addMiscMetadata(metadata.get(Metadata.CATEGORY))
				.addMiscMetadata(metadata.get(Metadata.COMMENT))
				.addMiscMetadata(metadata.get(Metadata.COMPANY))
				.addMiscMetadata(metadata.get(Metadata.MANAGER));
		}
		catch (AssertionError e) {
			/*
			 * With the RTF parser in Tika 0.10, calling TextExtractor.extract
			 * results in an AssertionError. See bug #3443948.
			 */
			throw new ParseException(e);
		}
		catch (Exception e) {
			throw new ParseException(e);
		}
	}
	
	protected String renderText(InputStream in, String filename)
			throws ParseException {
		BodyContentHandler bodyHandler = new BodyContentHandler(-1);
		Metadata metadata = new Metadata();
		XHTMLContentHandler handler = new XHTMLContentHandler(bodyHandler, metadata);
		TextExtractor extractor = new TextExtractor(handler, metadata);
		try {
			extractor.extract(in);
			return bodyHandler.toString();
		}
		catch (Exception e) {
			throw new ParseException(e);
		}
	}

	protected Collection<String> getExtensions() {
		return extensions;
	}

	protected Collection<String> getTypes() {
		return types;
	}

	public String getTypeLabel() {
		return Msg.filetype_rtf.get();
	}

}
