package org.iplantc.de.apps.integration.client.view.propertyEditors;

import static org.iplantc.de.apps.integration.shared.AppIntegrationModule.Ids;
import static org.iplantc.de.apps.integration.shared.AppIntegrationModule.PropertyPanelIds;

import org.iplantc.de.client.models.apps.integration.Argument;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextArea;

public class InfoPropertyEditor extends AbstractArgumentPropertyEditor {

    interface EditorDriver extends SimpleBeanEditorDriver<Argument, InfoPropertyEditor> {
    }

    interface InfoPropertyEditorUiBinder extends UiBinder<Widget, InfoPropertyEditor> {
    }

    @UiField(provided = true) PropertyEditorAppearance appearance;
    @UiField
    FieldLabel argLabelLabel;
    @UiField
    TextArea label;
    private static InfoPropertyEditorUiBinder uiBinder = GWT.create(InfoPropertyEditorUiBinder.class);
    private final EditorDriver editorDriver = GWT.create(EditorDriver.class);

    @Inject
    public InfoPropertyEditor(PropertyEditorAppearance appearance) {

        this.appearance = appearance;
        initWidget(uiBinder.createAndBindUi(this));
        argLabelLabel.setHTML(appearance.createContextualHelpLabel(appearance.infoLabel(), appearance.infoLabelHelp()));
        editorDriver.initialize(this);
        editorDriver.accept(new InitializeTwoWayBinding(this));
        ensureDebugId(Ids.PROPERTY_EDITOR + Ids.INFO);
    }

    @Override
    public void edit(Argument argument) {
        super.edit(argument);
        editorDriver.edit(argument);
    }

    @Override
    public com.google.gwt.editor.client.EditorDriver<Argument> getEditorDriver() {
        return editorDriver;
    }

    @Override
    protected void initLabelOnlyEditMode(boolean isLabelOnlyEditMode) {
        // Do nothing
    }

    @Override
    protected void onEnsureDebugId(String baseID) {
        super.onEnsureDebugId(baseID);
        label.ensureDebugId(baseID + PropertyPanelIds.LABEL);
    }
}
