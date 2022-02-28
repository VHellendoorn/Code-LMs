/*******************************************************************************
 * Copyright (c) 2010, 2011 Tran Nam Quang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tran Nam Quang - initial API and implementation
 *******************************************************************************/

package net.sourceforge.docfetcher.model.index;

import java.io.Closeable;
import java.io.IOException;

import net.sourceforge.docfetcher.model.Fields;
import net.sourceforge.docfetcher.model.IndexRegistry;
import net.sourceforge.docfetcher.util.CheckedOutOfMemoryError;
import net.sourceforge.docfetcher.util.annotations.NotNull;
import net.sourceforge.docfetcher.util.annotations.VisibleForPackageGroup;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;

import com.google.common.io.Closeables;

/**
 * Wrapper for Lucene's IndexWriter that adds some functionality.
 * 
 * @author Tran Nam Quang
 */
@VisibleForPackageGroup
public final class IndexWriterAdapter implements Closeable {
	
	public static final Term idTerm = new Term(Fields.UID.key());
	
	@NotNull private IndexWriter writer;

	public IndexWriterAdapter(@NotNull Directory luceneDir) throws IOException {
		writer = new IndexWriter(luceneDir, IndexRegistry.analyzer, MaxFieldLength.UNLIMITED);
	}

	// may throw OutOfMemoryError
	public void add(@NotNull Document document) throws IOException,
			CheckedOutOfMemoryError {
		try {
			writer.addDocument(document);
		}
		catch (OutOfMemoryError e) {
			reopenWriterAndThrow(e);
		}
	}

	// may throw OutOfMemoryError
	public void update(@NotNull String uid, @NotNull Document document)
			throws IOException, CheckedOutOfMemoryError {
		try {
			writer.updateDocument(idTerm.createTerm(uid), document);
		}
		catch (OutOfMemoryError e) {
			reopenWriterAndThrow(e);
		}
	}
	
	private void reopenWriterAndThrow(@NotNull OutOfMemoryError e)
			throws IOException, CheckedOutOfMemoryError {
		/*
		 * According to the IndexWriter javadoc, we're supposed to immediately
		 * close the IndexWriter if IndexWriter.addDocument(...) or
		 * IndexWriter.updateDocument(...) hit OutOfMemoryErrors.
		 */
		Directory indexDir = writer.getDirectory();
		Closeables.closeQuietly(writer);
		writer = new IndexWriter(indexDir, IndexRegistry.analyzer, MaxFieldLength.UNLIMITED);
		throw new CheckedOutOfMemoryError(e);
	}

	public void delete(@NotNull String uid) throws IOException {
		writer.deleteDocuments(idTerm.createTerm(uid));
	}
	
	public void close() throws IOException {
		writer.close();
	}

}
