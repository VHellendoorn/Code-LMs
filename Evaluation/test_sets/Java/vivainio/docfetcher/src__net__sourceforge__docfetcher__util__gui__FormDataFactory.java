/*******************************************************************************
 * Copyright (c) 2007, 2008 Tran Nam Quang.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Tran Nam Quang - initial API and implementation
 *******************************************************************************/

package net.sourceforge.docfetcher.util.gui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.widgets.Control;

/**
 * Helper class for creating SWT {@link org.eclipse.swt.layout.FormData
 * FormData} objects, similiar to the JFace
 * {@link org.eclipse.jface.layout.GridDataFactory GridDataFactory}. Usage:
 * <ul>
 * <li>Set a <tt>FormLayout</tt> for the parent composite via
 * {@link org.eclipse.swt.widgets.Composite#setLayout(org.eclipse.swt.widgets.Layout)
 * Composite.setLayout(Layout)}.
 * <li>Obtain the factory instance via {@link #getInstance()}. The factory
 * instance holds the <tt>FormData</tt> instance that will be applied to the
 * next control.
 * <li>Set the attachment points for the currently stored <tt>FormData</tt>
 * object via <tt>top()</tt>, <tt>bottom()</tt>, <tt>left()</tt> and
 * <tt>right()</tt>, and variants of these methods. The control can be attached
 * to the parent border (e.g. <tt>top()</tt>) or to the border of an adjacent
 * control (e.g. <tt>top(Control)</tt>). The variants of these methods allow
 * setting a specific offset to the border.
 * <li>If necessary, set a width and/or height hint via <tt>width(int)</tt> and
 * <tt>height(int)</tt>. Set a minimum width and/or height for the control via
 * <tt>minWidth(int)</tt> and <tt>minHeight(int)</tt>. Change the default margin
 * via <tt>margin(int)</tt>.
 * <li>Apply the current <tt>FormData</tt> to the control via
 * {@link #applyTo(Control)}.
 * <li>The current factory settings can be reused for subsequent controls. At
 * some point it may be necessary to reset the factory via {@link #reset()}.
 * Note that obtaining the factory via <tt>getInstance()</tt> will also reset
 * it.
 * <li>The above methods to configure the <tt>FormData</tt> can be chained since
 * they return the factory instance.
 * </ul>
 * 
 * @author Tran Nam Quang
 */
public final class FormDataFactory {
	
	/** Singleton instance */
	private static FormDataFactory instance;
	
	/**
	 * The margin to use if {@link #top()}, {@link #bottom()}, {@link #left()}
	 * or {@link #right()} are called.
	 */
	public static final int DEFAULT_MARGIN = 5;
	
	// Current form data settings
	private FormData fd = new FormData();
	private int margin = DEFAULT_MARGIN;
	private int minWidth = 0;
	private int minHeight = 0;
	
	private FormDataFactory() {
		// Singleton
	}
	
	/**
	 * Returns the instance of this class with all fields reset.
	 */
	public static FormDataFactory getInstance() {
		if (instance == null)
			 instance = new FormDataFactory();
		return instance.reset();
	}
	
	/**
	 * Creates a copy of the currently stored FormData object and applies the
	 * copy to this control.
	 */
	public FormDataFactory applyTo(Control control) {
		Point defaultSize = null;
		if (minWidth > 0 || minHeight > 0) {
			defaultSize = control.computeSize(SWT.DEFAULT, SWT.DEFAULT);
			if (minWidth > 0)
				fd.width = Math.max(minWidth, defaultSize.x);
			if (minHeight > 0)
				fd.height = Math.max(minHeight, defaultSize.y);
		}
		control.setLayoutData(fd);
		FormData oldFormData = fd;
		fd = new FormData();
		fd.width = oldFormData.width;
		fd.height= oldFormData.height;
		fd.top = oldFormData.top;
		fd.bottom = oldFormData.bottom;
		fd.left = oldFormData.left;
		fd.right = oldFormData.right;
		return this;
	}
	
	/**
	 * Creates a FormData object for the current state of the factory.
	 */
	public FormData create() {
		FormData ret = new FormData();
		ret.width = fd.width;
		ret.height= fd.height;
		ret.top = fd.top;
		ret.bottom = fd.bottom;
		ret.left = fd.left;
		ret.right = fd.right;
		return ret;
	}
	
	/**
	 * Resets all <tt>FormData</tt> fields. The margin is reset to
	 * {@link #DEFAULT_MARGIN}.
	 */
	public FormDataFactory reset() {
		fd = new FormData();
		margin = DEFAULT_MARGIN;
		minWidth = 0;
		minHeight = 0;
		return this;
	}
	
	/**
	 * Sets the margin to be used for subsequently created <tt>FormData</tt>
	 * objects.
	 */
	public FormDataFactory margin(int margin) {
		this.margin = margin;
		return this;
	}
	
	/**
	 * Returns the current margin.
	 */
	public int getMargin() {
		return margin;
	}
	
	public FormDataFactory top(int numerator, int offset) {
		fd.top = new FormAttachment(numerator, offset);
		return this;
	}
	
	public FormDataFactory top() {
		fd.top = new FormAttachment(0, margin);
		return this;
	}
	
	public FormDataFactory untop() {
		fd.top = null;
		return this;
	}
	
	public FormDataFactory bottom(int numerator, int offset) {
		fd.bottom = new FormAttachment(numerator, offset);
		return this;
	}
	
	public FormDataFactory bottom() {
		fd.bottom = new FormAttachment(100, -margin);
		return this;
	}
	
	public FormDataFactory unbottom() {
		fd.bottom = null;
		return this;
	}
	
	public FormDataFactory left(int numerator, int offset) {
		fd.left = new FormAttachment(numerator, offset);
		return this;
	}

	public FormDataFactory left() {
		fd.left = new FormAttachment(0, margin);
		return this;
	}
	
	public FormDataFactory unleft() {
		fd.left = null;
		return this;
	}
	
	public FormDataFactory right(int numerator, int offset) {
		fd.right = new FormAttachment(numerator, offset);
		return this;
	}

	public FormDataFactory right() {
		fd.right = new FormAttachment(100, -margin);
		return this;
	}
	
	public FormDataFactory unright() {
		fd.right = null;
		return this;
	}

	public FormDataFactory top(Control control, int offset) {
		fd.top = new FormAttachment(control, offset);
		return this;
	}
	
	public FormDataFactory top(Control control) {
		fd.top = new FormAttachment(control, margin);
		return this;
	}
	
	public FormDataFactory bottom(Control control, int offset) {
		fd.bottom = new FormAttachment(control, offset);
		return this;
	}

	public FormDataFactory bottom(Control control) {
		fd.bottom = new FormAttachment(control, -margin);
		return this;
	}
	
	public FormDataFactory left(Control control, int offset) {
		fd.left = new FormAttachment(control, offset);
		return this;
	}

	public FormDataFactory left(Control control) {
		fd.left = new FormAttachment(control, margin);
		return this;
	}
	
	public FormDataFactory right(Control control, int offset) {
		fd.right = new FormAttachment(control, offset);
		return this;
	}

	public FormDataFactory right(Control control) {
		fd.right = new FormAttachment(control, -margin);
		return this;
	}
	
	public FormDataFactory width(int width) {
		fd.width = width;
		return this;
	}
	
	public FormDataFactory unwidth() {
		fd.width = SWT.DEFAULT;
		return this;
	}
	
	public FormDataFactory height(int height) {
		fd.height = height;
		return this;
	}
	
	public FormDataFactory unheight() {
		fd.height = SWT.DEFAULT;
		return this;
	}
	
	/**
	 * The minimum width of the control. Can be set to 0 to remove any previous
	 * minimum setting.
	 */
	public FormDataFactory minWidth(int width) {
		minWidth = width;
		return this;
	}
	
	/**
	 * The minimum height of the control. Can be set to 0 to remove any previous
	 * minimum setting.
	 */
	public FormDataFactory minHeight(int height) {
		minHeight = height;
		return this;
	}

}
