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

package net.sourceforge.docfetcher.model.index;

import net.sourceforge.docfetcher.util.annotations.VisibleForPackageGroup;

/**
 * @author Tran Nam Quang
 */
@VisibleForPackageGroup
public final class MutableInt {
	
	private int value;
	
	public MutableInt(int value) {
		this.value = value;
	}

	public int get() {
		return value;
	}
	
	public void set(int value) {
		this.value = value;
	}
	
	public void increment() {
		value++;
	}

}
