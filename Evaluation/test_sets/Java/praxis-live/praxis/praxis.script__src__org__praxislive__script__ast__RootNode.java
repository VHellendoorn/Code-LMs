/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2018 Neil C Smith.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this work; if not, see http://www.gnu.org/licenses/
 * 
 *
 * Please visit https://www.praxislive.org if you need additional information or
 * have any questions.
 */

package org.praxislive.script.ast;

import java.util.List;
import org.praxislive.core.Value;
import org.praxislive.script.ExecutionException;

/**
 *
 * @author Neil C Smith (http://neilcsmith.net)
 */
public class RootNode extends CompositeNode {

    public RootNode(List<LineNode> children) {
        super(children);
    }

    @Override
    protected boolean isThisDone() {
        return true;
    }

    @Override
    protected void writeThisNextCommand(List<Value> args) {
        throw new IllegalStateException();
    }

    @Override
    protected void postThisResponse(List<Value> args) {
        throw new IllegalStateException();
    }

    @Override
    public void writeResult(List<Value> args) throws ExecutionException {
        Node[] children = getChildren();
        if (children.length > 0) {
            children[children.length - 1].writeResult(args);
        }
        
    }



}
