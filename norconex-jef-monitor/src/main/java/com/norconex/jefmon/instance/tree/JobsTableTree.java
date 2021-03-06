/* Copyright 2007-2015 Norconex Inc.
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
package com.norconex.jefmon.instance.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.tree.DefaultTableTree;
import org.apache.wicket.extensions.markup.html.repeater.tree.Node;
import org.apache.wicket.extensions.markup.html.repeater.tree.table.TreeColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import com.norconex.commons.wicket.behaviors.CssClass;

public class JobsTableTree extends DefaultTableTree<JobStatusTreeNode, Void>{

    private static final long serialVersionUID = 6386274038061059124L;

    public JobsTableTree(String id,
            JobTreeProvider provider,
            int rowsPerPage, 
            IModel<Set<JobStatusTreeNode>> state) {
        super(id, createColumns(), provider, rowsPerPage, state);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();
        getTable().add(new CssClass("table table-condensed small"));
    }
    
    @Override
    public Component newNodeComponent(
            String id, IModel<JobStatusTreeNode> model) {
        Component node = new Node<JobStatusTreeNode>(id, this, model) {
            private static final long serialVersionUID = -5096730351024888675L;
            @Override
            protected Component createContent(
                    String id, IModel<JobStatusTreeNode> model) {
                return newContentComponent(id, model);
            }
            @Override
            protected String getCollapsedStyleClass() {
                return "jef-tree-node-collapsed";
            }
            @Override
            protected String getExpandedStyleClass(JobStatusTreeNode t) {
                return "jef-tree-node-expanded";
            }
            @Override
            protected String getOtherStyleClass() {
                return super.getOtherStyleClass();
            }
            @Override
            protected void onBeforeRender() {
                super.onBeforeRender();
            }
        };
        node.setOutputMarkupId(true);
        return node;
    }
    
    @Override
    protected Component newContentComponent(String id,
            IModel<JobStatusTreeNode> model) {
        return new JEFFolder(id, this, model);
    }

    private static List<IColumn<JobStatusTreeNode, Void>> createColumns() {
        List<IColumn<JobStatusTreeNode, Void>> columns =
                new ArrayList<IColumn<JobStatusTreeNode, Void>>();
        columns.add(new TreeColumn<JobStatusTreeNode, Void>(
                new ResourceModel("col.job")));
        columns.add(new ProgressColumn(new ResourceModel("col.progress")));
        columns.add(new StatusColumn(new ResourceModel("col.status")));
        columns.add(new DateStartedColumn(new ResourceModel("col.started")));
        columns.add(new DurationColumn(new ResourceModel("col.duration")));
        columns.add(new RemainingColumn(new ResourceModel("col.remaining")));
        columns.add(new PropertyColumn<JobStatusTreeNode, Void>(
                new ResourceModel("col.note"), "note"));

        columns.add(new ActionsColumn(new ResourceModel("col.actions")));

        return columns;
    }
}
