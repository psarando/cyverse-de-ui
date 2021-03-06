package org.iplantc.de.diskResource.client.views.dataLink.cells;

import static com.google.gwt.dom.client.BrowserEvents.CLICK;

import org.iplantc.de.client.models.dataLink.DataLink;
import org.iplantc.de.client.models.diskResources.DiskResource;
import org.iplantc.de.diskResource.client.events.selection.DeleteDataLinkSelected;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.shared.HandlerManager;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.Event;
import com.google.inject.Inject;

/**
 * @author jstroot
 */
public final class DataLinkPanelCell extends AbstractCell<DiskResource> implements DeleteDataLinkSelected.HasDeleteDataLinkSelectedHandlers {

    public interface Appearance {
        void render(SafeHtmlBuilder sb, DiskResource value);
    }

    private final Appearance appearance;
    private HandlerManager handlerManager;

    @Inject
    public DataLinkPanelCell(final Appearance appearance) {
        super(CLICK);
        this.appearance = appearance;
    }

    @Override
    public HandlerRegistration addDeleteDataLinkSelectedHandler(DeleteDataLinkSelected.DeleteDataLinkSelectedHandler handler) {
        return ensureHandlers().addHandler(DeleteDataLinkSelected.TYPE, handler);
    }

    @Override
    public void onBrowserEvent(Cell.Context context, Element parent, DiskResource value,
                               NativeEvent event,
                               ValueUpdater<DiskResource> valueUpdater) {

        if (value == null) {
            return;
        }

        Element eventTarget = Element.as(event.getEventTarget());
        if (parent.isOrHasChild(eventTarget)) {

            switch (Event.as(event).getTypeInt()) {
                case Event.ONCLICK:
                    doOnClick(eventTarget, value);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void render(Cell.Context context, DiskResource value, SafeHtmlBuilder sb) {
        appearance.render(sb, value);
    }

    private void doOnClick(Element eventTarget, DiskResource value) {
        if (eventTarget.getAttribute("name").equalsIgnoreCase("del")) {
            ensureHandlers().fireEvent(new DeleteDataLinkSelected((DataLink) value));
        }
    }

    HandlerManager createHandlerManager() {
        return new HandlerManager(this);
    }

    HandlerManager ensureHandlers() {
        return handlerManager == null ? handlerManager = createHandlerManager() : handlerManager;
    }

    HandlerManager getHandlerManager() {
        return handlerManager;
    }
}
