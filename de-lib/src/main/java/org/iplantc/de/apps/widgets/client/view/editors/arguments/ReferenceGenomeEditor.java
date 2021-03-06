package org.iplantc.de.apps.widgets.client.view.editors.arguments;

import static com.sencha.gxt.cell.core.client.form.ComboBoxCell.TriggerAction.ALL;

import org.iplantc.de.apps.shared.AppsModule;
import org.iplantc.de.apps.widgets.client.models.ReferenceGenomeProperties;
import org.iplantc.de.apps.widgets.client.view.editors.arguments.converters.ArgumentEditorConverter;
import org.iplantc.de.apps.widgets.client.view.editors.arguments.converters.ReferenceGenomeEditorConverter;
import org.iplantc.de.apps.widgets.client.view.editors.arguments.converters.SplittableToReferenceGenomeConverter;
import org.iplantc.de.apps.widgets.client.view.editors.style.AppTemplateWizardAppearance;
import org.iplantc.de.client.models.apps.refGenome.ReferenceGenome;

import com.google.gwt.event.logical.shared.HasValueChangeHandlers;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.google.web.bindery.autobean.shared.Splittable;

import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.widget.core.client.form.ComboBox;

public class ReferenceGenomeEditor extends AbstractArgumentEditor implements HasValueChangeHandlers<Splittable> {
    private final ComboBox<ReferenceGenome> comboBox;
    private final ArgumentEditorConverter<ReferenceGenome> editorAdapter;

    @Inject
    public ReferenceGenomeEditor(@Assisted AppTemplateWizardAppearance appearance,
                                 @Assisted ListStore<ReferenceGenome> refGenomeStore,
                                 ReferenceGenomeProperties props) {
        super(appearance);
        comboBox = new ComboBox<ReferenceGenome>(refGenomeStore, props.name());
        comboBox.setTriggerAction(ALL);
        comboBox.setEmptyText(appearance.emptyListSelectionText());
        comboBox.setMinChars(1);
        ClearComboBoxSelectionKeyDownHandler handler = new ClearComboBoxSelectionKeyDownHandler(comboBox);
        comboBox.addKeyDownHandler(handler);

        editorAdapter = new ReferenceGenomeEditorConverter<>(comboBox, new SplittableToReferenceGenomeConverter());

        argumentLabel.setWidget(editorAdapter);
    }

    @Override
    public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Splittable> handler) {
        return editorAdapter.addValueChangeHandler(handler);
    }

    @Override
    public ArgumentEditorConverter<?> valueEditor() {
        return editorAdapter;
    }

    @Override
    protected void onEnsureDebugId(String baseID) {
        super.onEnsureDebugId(baseID);

        comboBox.setId(baseID + AppsModule.Ids.APP_LAUNCH_REFERENCE_GENOME);
    }
}
