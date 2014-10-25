package com.norconex.jefmon.instance.action;

import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.link.PopupSettings;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.Model;

import com.norconex.commons.wicket.behaviors.CssClass;
import com.norconex.commons.wicket.bootstrap.tooltip.BootstrapTooltip;
import com.norconex.jefmon.JEFMonPanel;
import com.norconex.jefmon.instance.tree.JobStatusTreeNode;

/**
 * Tree table cell showing action icons.
 * @author Pascal Essiembre
 */
public class ActionsCell extends JEFMonPanel {

    private static final long serialVersionUID = -8687324636771116674L;
    
    @SuppressWarnings("nls")
    public ActionsCell(final String id, 
            final WebMarkupContainer dialogWrapper,
            final String dialogId, 
            final JobStatusTreeNode job, final List<IJobAction> actions) {
        super(id);
        
        add(new ListView<IJobAction>("actions", actions) {
            private static final long serialVersionUID = 3635147316426384496L;
            @Override
            protected void populateItem(ListItem<IJobAction> item) {
                final IJobAction action = item.getModelObject();
                if (action.isVisible(job)) {
                    String icon = action.getFontIcon();
                    final String pageName = action.getName().getObject() 
                            + " - " + job.getJobId();
                    Link<String> link = new Link<String>("actionLink") {
                        private static final long serialVersionUID = 
                                3488334322744811811L;
                        @Override
                        public void onClick() {
                            Component comp = action.execute(
                                    job, ActionPage.COMPONENT_ID);
                            if (comp != null) {
                                setResponsePage(new ActionPage(
                                        comp, Model.of(pageName)));
                            }
                        }
                    };
                    PopupSettings popup = new PopupSettings(
                            pageName, PopupSettings.RESIZABLE);
                    popup.setWidth(800);
                    link.setPopupSettings(popup);
                    link.add(new Label("actionIcon").add(new CssClass(icon)));
//                    link.add(new Title(action.getName()));
                    link.add(new BootstrapTooltip(action.getName()));
                    item.add(link);
                } else {
                    
                    WebMarkupContainer empty = 
                            new WebMarkupContainer("actionLink");
                    empty.add(new Label("actionIcon"));
                    empty.setVisible(false);
                    item.add(empty);
                }
            }
        });
    }
}