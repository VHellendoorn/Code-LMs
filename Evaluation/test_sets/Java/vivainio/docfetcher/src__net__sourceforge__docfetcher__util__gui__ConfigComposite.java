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

package net.sourceforge.docfetcher.util.gui;

import net.sourceforge.docfetcher.util.Util;
import net.sourceforge.docfetcher.util.annotations.NotNull;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * @author Tran Nam Quang
 */
public abstract class ConfigComposite extends Composite {

	public static void main(String[] args) {
		Display display = new Display();
		Shell shell = new Shell(display);
		shell.setLayout(new FillLayout());

		new ConfigComposite(shell, SWT.H_SCROLL) {
			protected Control createContents(Composite parent) {
				Composite comp = new Composite(parent, SWT.NONE);
				comp.setLayout(Util.createGridLayout(5, true, 5, 5));
				for (int i = 0; i < 5; i++) {
					for (int j = 0; j < 5; j++) {
						Button bt = new Button(comp, SWT.PUSH);
						bt.setText(String.format("Button (%d, %d)", i, j));
						bt.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
					}
				}
				return comp;
			}
			protected Control createButtonArea(Composite parent) {
				return new Label(parent, SWT.NONE);
			}
		};

		Util.setCenteredBounds(shell, 300, 250);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
	
	// style is a combination of SWT.H_SCROLL and SWT.V_SCROLL
	public ConfigComposite(@NotNull Composite parent, final int style) {
		super(parent, SWT.NONE);
		
		final boolean hScroll = Util.contains(style, SWT.H_SCROLL);
		final boolean vScroll = Util.contains(style, SWT.V_SCROLL);
		
		final ScrolledComposite scrollComp = new ScrolledComposite(this, style);
		scrollComp.setExpandHorizontal(true);
		scrollComp.setExpandVertical(true);
		
		final Control contents = createContents(scrollComp);
		scrollComp.setContent(contents);
		
		if (hScroll || vScroll) {
			scrollComp.addControlListener(new ControlAdapter() {
				public void controlResized(ControlEvent e) {
					Rectangle r = scrollComp.getClientArea();
					int wHint = hScroll ? SWT.DEFAULT : r.width;
					int hHint = vScroll ? SWT.DEFAULT : r.height;
					Point size = contents.computeSize(wHint, hHint);
					scrollComp.setMinSize(size);
				}
			});
		}
		
		Label separator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		Control buttonArea = createButtonArea(this);
		
		setLayout(Util.createGridLayout(1, false, 0, 5));
		scrollComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		separator.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		buttonArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
	}
	
	@NotNull
	protected abstract Control createContents(@NotNull Composite parent);
	
	@NotNull
	protected abstract Control createButtonArea(@NotNull Composite parent);

}
