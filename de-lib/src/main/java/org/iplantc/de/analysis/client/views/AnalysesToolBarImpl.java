package org.iplantc.de.analysis.client.views;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.uibinder.client.*;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;
import com.sencha.gxt.data.shared.StringLabelProvider;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.Composite;
import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.SimpleComboBox;
import com.sencha.gxt.widget.core.client.menu.Item;
import com.sencha.gxt.widget.core.client.menu.MenuItem;
import com.sencha.gxt.widget.core.client.selection.SelectionChangedEvent;
import com.sencha.gxt.widget.core.client.toolbar.ToolBar;
import org.iplantc.de.analysis.client.AnalysesView;
import org.iplantc.de.analysis.client.AnalysisToolBarView;
import org.iplantc.de.analysis.client.events.AnalysisCommentUpdate;
import org.iplantc.de.analysis.client.events.AnalysisFilterChanged;
import org.iplantc.de.analysis.client.events.selection.*;
import org.iplantc.de.analysis.client.views.widget.AnalysisSearchField;
import org.iplantc.de.analysis.shared.AnalysisModule;
import org.iplantc.de.client.models.AnalysisTypeFilter;
import org.iplantc.de.client.models.UserInfo;
import org.iplantc.de.client.models.analysis.Analysis;
import org.iplantc.de.client.models.analysis.AnalysisPermissionFilter;

import java.util.Arrays;
import java.util.List;

import static org.iplantc.de.client.models.analysis.AnalysisExecutionStatus.*;

/**
 * @author sriram, jstroot
 */
public class AnalysesToolBarImpl extends Composite implements AnalysisToolBarView {

    @UiTemplate("AnalysesToolBarImpl.ui.xml")
    interface AnalysesToolbarUiBinder extends UiBinder<Widget, AnalysesToolBarImpl> { }

    @UiField ToolBar menuBar;
    @UiField MenuItem goToFolderMI;
    @UiField MenuItem viewParamsMI;
    @UiField
    MenuItem viewJobInfoMI;
    @UiField MenuItem relaunchMI;
    @UiField MenuItem cancelMI;
    @UiField MenuItem deleteMI;
    @UiField TextButton analysesTb;
    @UiField MenuItem updateCommentsMI;
    @UiField MenuItem renameMI;
    @UiField TextButton refreshTb;
    @UiField AnalysisSearchField searchField;
    @UiField(provided = true) final AnalysesView.Appearance appearance;
    @UiField
    MenuItem shareCollabMI;

    @UiField(provided = true)
    SimpleComboBox<AnalysisPermissionFilter> filterPermCombo;

    @UiField(provided = true)
    SimpleComboBox<AnalysisTypeFilter> filterTypeCombo;


    @Inject
    UserInfo userInfo;


    List<Analysis> currentSelection;
    private final PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Analysis>> loader;

    @Inject
    AnalysesToolBarImpl(final AnalysesView.Appearance appearance,
                        @Assisted PagingLoader<FilterPagingLoadConfig, PagingLoadResult<Analysis>> loader) {
        this.appearance = appearance;
        this.loader = loader;

        filterPermCombo = new SimpleComboBox<>(new StringLabelProvider<>());
        filterPermCombo.add(Arrays.asList(AnalysisPermissionFilter.ALL,
                AnalysisPermissionFilter.MY_ANALYSES,
                AnalysisPermissionFilter.SHARED_WITH_ME));
        filterTypeCombo = new SimpleComboBox<>(new StringLabelProvider<>());
        filterTypeCombo.add(Arrays.asList(AnalysisTypeFilter.AGAVE,
                AnalysisTypeFilter.DE,
                AnalysisTypeFilter.INTERACTIVE,
                AnalysisTypeFilter.OSG));
        AnalysesToolbarUiBinder uiBinder = GWT.create(AnalysesToolbarUiBinder.class);
        initWidget(uiBinder.createAndBindUi(this));
        filterPermCombo.setEditable(false);
        filterPermCombo.setValue(AnalysisPermissionFilter.ALL);
        filterPermCombo.addSelectionHandler(event -> {
            onPermFilterChange(event.getSelectedItem());
            searchField.clear();
        });
        filterPermCombo.addValueChangeHandler(event -> onPermFilterChange(event.getValue()));


        filterTypeCombo.setEditable(false);
        filterTypeCombo.setValue(AnalysisTypeFilter.DE);
        filterTypeCombo.addSelectionHandler(event -> {
            onTypeFilterChange(event.getSelectedItem());
            searchField.clear();
        });
        filterTypeCombo.addValueChangeHandler(event -> onTypeFilterChange(event.getValue()));


        searchField.addHideHandler(handler -> searchField.setVisible(true));
    }

    private void onPermFilterChange(AnalysisPermissionFilter af) {
        switch (af) {
            case ALL:
                applyFilter(AnalysisPermissionFilter.ALL);
                break;
            case SHARED_WITH_ME:
                applyFilter(AnalysisPermissionFilter.SHARED_WITH_ME);
                break;

            case MY_ANALYSES:
                applyFilter(AnalysisPermissionFilter.MY_ANALYSES);
                break;
        }
    }

    private void onTypeFilterChange(AnalysisTypeFilter af) {
        switch (af) {
            case AGAVE:
                break;
            case DE:
                break;
            case INTERACTIVE:
                break;
            case OSG:
                break;
        }
    }

    @UiFactory
    AnalysisSearchField createSearchField() {
        return new AnalysisSearchField(loader);
    }

    @Override
    public void filterByAnalysisId(String analysisId, String name) {
        searchField.filterByAnalysisId(analysisId, name);
        //reset filter. Users need to set Filter to ALL to go back...
        filterPermCombo.setValue(null);
        applyFilter(null);
    }

    @Override
    public void filterByParentAnalysisId(String analysisId) {
        searchField.filterByParentId(analysisId);
        //reset filter. Users need to set Filter to ALL to go back...
        filterPermCombo.setValue(null);
        applyFilter(null);
    }

    @Override
    public void onSelectionChanged(SelectionChangedEvent<Analysis> event) {
        currentSelection = event.getSelection();

        int size = currentSelection.size();
        final boolean canCancelSelection = canCancelSelection(currentSelection);
        final boolean canDeleteSelection = canDeleteSelection(currentSelection);
        boolean isOwner = isOwner(currentSelection);
        boolean can_share = isShareable(currentSelection);

        boolean goToFolderEnabled, viewParamsEnabled, relaunchEnabled, cancelEnabled, deleteEnabled;
        boolean renameEnabled, updateCommentsEnabled, shareEnabled;
        switch (size) {
            case 0:
                goToFolderEnabled = false;
                viewParamsEnabled = false;
                relaunchEnabled = false;
                cancelEnabled = false;
                deleteEnabled = false;

                renameEnabled = false;
                updateCommentsEnabled = false;
                shareEnabled = false;

                break;
            case 1:
                goToFolderEnabled = true;
                viewParamsEnabled = true;
                relaunchEnabled = !currentSelection.get(0).isAppDisabled();
                cancelEnabled = canCancelSelection && isOwner;
                deleteEnabled = canDeleteSelection && isOwner;

                renameEnabled = isOwner;
                updateCommentsEnabled = isOwner;
                shareEnabled = isOwner && can_share;
                break;

            default:
                // If more than 1 is selected
                goToFolderEnabled = false;
                viewParamsEnabled = false;
                relaunchEnabled = false;
                cancelEnabled = canCancelSelection && isOwner;
                deleteEnabled = canDeleteSelection && isOwner;
                shareEnabled = isOwner && can_share;
                renameEnabled = false;
                updateCommentsEnabled = false;
        }

        goToFolderMI.setEnabled(goToFolderEnabled);
        viewParamsMI.setEnabled(viewParamsEnabled);
        viewJobInfoMI.setEnabled(viewParamsEnabled);
        relaunchMI.setEnabled(relaunchEnabled);
        cancelMI.setEnabled(cancelEnabled);
        deleteMI.setEnabled(deleteEnabled);
        shareCollabMI.setEnabled(shareEnabled);
       // shareSupportMI.setEnabled(shareEnabled);
        renameMI.setEnabled(renameEnabled);
        updateCommentsMI.setEnabled(updateCommentsEnabled);
    }

    private boolean isOwner(List<Analysis> selection) {
        for (Analysis a : selection) {
            if (!(a.getUserName().equals(userInfo.getFullUsername()))) {
                return false;
            }
        }

        return true;
    }

    boolean isShareable(List<Analysis> selection) {
        for (Analysis a : selection) {
            if (!(a.isShareable())) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected void onEnsureDebugId(String baseID) {
        super.onEnsureDebugId(baseID);
        // Analysis menu
        analysesTb.ensureDebugId(baseID + AnalysisModule.Ids.MENUITEM_ANALYSES);
        goToFolderMI.ensureDebugId(baseID + AnalysisModule.Ids.MENUITEM_ANALYSES
                + AnalysisModule.Ids.MENUITEM_GO_TO_FOLDER);
        viewParamsMI.ensureDebugId(baseID + AnalysisModule.Ids.MENUITEM_ANALYSES
                + AnalysisModule.Ids.MENUITEM_VIEW_PARAMS);
        viewJobInfoMI.ensureDebugId(baseID + AnalysisModule.Ids.MENUITEM_ANALYSES
                + AnalysisModule.Ids.MENUITEM_VIEW_ANALYSES_INFO);
        relaunchMI.ensureDebugId(baseID + AnalysisModule.Ids.MENUITEM_ANALYSES
                + AnalysisModule.Ids.MENUITEM_RELAUNCH);
        cancelMI.ensureDebugId(baseID + AnalysisModule.Ids.MENUITEM_ANALYSES
                + AnalysisModule.Ids.MENUITEM_CANCEL);
        deleteMI.ensureDebugId(baseID + AnalysisModule.Ids.MENUITEM_ANALYSES
                + AnalysisModule.Ids.MENUITEM_DELETE);

        renameMI.ensureDebugId(baseID + AnalysisModule.Ids.MENUITEM_EDIT
                + AnalysisModule.Ids.MENUITEM_RENAME);
        updateCommentsMI.ensureDebugId(baseID + AnalysisModule.Ids.MENUITEM_EDIT
                + AnalysisModule.Ids.MENUITEM_UPDATE_COMMENTS);

        refreshTb.ensureDebugId(baseID + AnalysisModule.Ids.BUTTON_REFRESH);
        searchField.ensureDebugId(baseID + AnalysisModule.Ids.FIELD_SEARCH);
        shareCollabMI.ensureDebugId(baseID + AnalysisModule.Ids.SHARE_COLLAB);
        shareCollabMI.ensureDebugId(baseID + AnalysisModule.Ids.SHARE_SUPPORT);
    }

    /**
     * Determines if the cancel button should be enable for the given selection.
     *
     * @return true if the selection contains ANY uncompleted batch jobs or status which is SUBMITTED, IDLE, or RUNNING;
     *         false otherwise.
     */
    boolean canCancelSelection(final List<Analysis> selection) {
        if (selection == null) {
            return false;
        }

        for (Analysis ae : selection) {
            if (ae == null) {
                continue;
            }

            final String status = ae.getStatus();
            if (SUBMITTED.toString().equalsIgnoreCase(status)
                    || IDLE.toString().equalsIgnoreCase(status)
                    || RUNNING.toString().equalsIgnoreCase(status)) {
                return true;
            }

            if (ae.isBatch() && (ae.getBatchStatus().getSubmitted() > 0
                                 || ae.getBatchStatus().getRunning() > 0)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if the delete button should be enabled for the given selection.
     * 
     * @return true if the selection ONLY contains FAILED or COMPLETED status, false otherwise.
     */
    boolean canDeleteSelection(List<Analysis> selection) {
        for (Analysis ae : selection) {
            if (ae == null)
                continue;

            final String status = ae.getStatus();
            if (!(FAILED.toString().equalsIgnoreCase(status)
                    || COMPLETED.toString().equalsIgnoreCase(status)
                      || CANCELED.toString().equalsIgnoreCase(status))) {
                return false;
            }

        }
        return true;
    }

    //<editor-fold desc="Ui Handlers">
    @UiHandler("searchField")
    void searchFieldKeyUp(KeyUpEvent event){
        if (Strings.isNullOrEmpty(searchField.getCurrentValue())) {
            filterPermCombo.setValue(AnalysisPermissionFilter.ALL);
        } else {
            filterPermCombo.setValue(null);
            applyFilter(null);
        }
    }


    @UiHandler("cancelMI")
    void onCancelSelected(SelectionEvent<Item> event) {
        Preconditions.checkNotNull(currentSelection);
        Preconditions.checkState(!currentSelection.isEmpty());

        fireEvent(new CancelAnalysisSelected(currentSelection));
    }

    @UiHandler("deleteMI")
    void onDeleteSelected(SelectionEvent<Item> event) {
        Preconditions.checkNotNull(currentSelection);
        Preconditions.checkState(!currentSelection.isEmpty());

        fireEvent(new DeleteAnalysisSelected(currentSelection));
    }

    @UiHandler("goToFolderMI")
    void onGoToFolderSelected(SelectionEvent<Item> event) {
        Preconditions.checkNotNull(currentSelection);
        Preconditions.checkState(currentSelection.size() == 1);

        fireEvent(new GoToAnalysisFolderSelected(currentSelection.iterator().next()));
    }

    @UiHandler("relaunchMI")
    void onRelaunchSelected(SelectionEvent<Item> event) {
        Preconditions.checkNotNull(currentSelection);
        Preconditions.checkState(currentSelection.size() == 1);
        fireEvent(new RelaunchAnalysisSelected(currentSelection.iterator().next()));
    }

    @UiHandler("renameMI")
    void onRenameSelected(SelectionEvent<Item> event) {
        Preconditions.checkNotNull(currentSelection);
        Preconditions.checkState(currentSelection.size() == 1);

        final Analysis selectedAnalysis = currentSelection.iterator().next();

        fireEvent(new RenameAnalysisSelected(selectedAnalysis));
    }

    @UiHandler("updateCommentsMI")
    void onUpdateCommentsSelected(SelectionEvent<Item> event) {
        Preconditions.checkNotNull(currentSelection);
        Preconditions.checkState(currentSelection.size() == 1,
                                 "There should only be 1 analysis selected, but there were %i",
                                 currentSelection.size());


        final Analysis selectedAnalysis = currentSelection.iterator().next();
        fireEvent(new AnalysisCommentUpdate(selectedAnalysis));
    }

    @UiHandler("viewParamsMI")
    void onViewParamsSelected(SelectionEvent<Item> event) {
        Preconditions.checkNotNull(currentSelection);
        Preconditions.checkState(currentSelection.size() == 1);

        fireEvent(new ViewAnalysisParamsSelected(currentSelection.get(0)));
    }

    @UiHandler("viewJobInfoMI")
    void onViewAnalysisStepsInfo(SelectionEvent<Item> event) {
        fireEvent(new AnalysisJobInfoSelected(currentSelection.get(0)));
    }

    @UiHandler("refreshTb")
    void onRefreshSelected(SelectEvent event) {
        fireEvent(new RefreshAnalysesSelected());
    }

    void applyFilter(AnalysisPermissionFilter filter) {
        fireEvent(new AnalysisFilterChanged(filter));
    }

    @Override
    public void setFilterInView(AnalysisPermissionFilter filter) {
        filterPermCombo.setValue(filter);
    }

    @Override
    public AnalysisSearchField getSearchField() {
        return searchField;
    }

    @UiHandler("shareCollabMI")
    void onShareSelected(SelectionEvent<Item> event) {
        fireEvent(new ShareAnalysisSelected(currentSelection));
    }

    @Override
    public HandlerRegistration addAnalysisJobInfoSelectedHandler(AnalysisJobInfoSelected.AnalysisJobInfoSelectedHandler handler) {
        return addHandler(handler, AnalysisJobInfoSelected.TYPE);
    }

    @Override
    public HandlerRegistration addAnalysisCommentUpdateHandler(AnalysisCommentUpdate.AnalysisCommentUpdateHandler handler) {
        return addHandler(handler, AnalysisCommentUpdate.TYPE);
    }

    @Override
    public HandlerRegistration addShareAnalysisSelectedHandler(ShareAnalysisSelected.ShareAnalysisSelectedHandler handler) {
        return addHandler(handler, ShareAnalysisSelected.TYPE);
    }

    @Override
    public HandlerRegistration addAnalysisFilterChangedHandler(AnalysisFilterChanged.AnalysisFilterChangedHandler handler) {
        return addHandler(handler, AnalysisFilterChanged.TYPE);
    }

    @Override
    public HandlerRegistration addRefreshAnalysesSelectedHandler(RefreshAnalysesSelected.RefreshAnalysesSelectedHandler handler) {
        return addHandler(handler, RefreshAnalysesSelected.TYPE);
    }

    @Override
    public HandlerRegistration addRenameAnalysisSelectedHandler(RenameAnalysisSelected.RenameAnalysisSelectedHandler handler) {
        return addHandler(handler, RenameAnalysisSelected.TYPE);
    }

    @Override
    public HandlerRegistration addRelaunchAnalysisSelectedHandler(RelaunchAnalysisSelected.RelaunchAnalysisSelectedHandler handler) {
        return addHandler(handler, RelaunchAnalysisSelected.TYPE);
    }

    @Override
    public HandlerRegistration addGoToAnalysisFolderSelectedHandler(GoToAnalysisFolderSelected.GoToAnalysisFolderSelectedHandler handler) {
        return addHandler(handler, GoToAnalysisFolderSelected.TYPE);
    }

    @Override
    public HandlerRegistration addDeleteAnalysisSelectedHandler(DeleteAnalysisSelected.DeleteAnalysisSelectedHandler handler) {
        return addHandler(handler, DeleteAnalysisSelected.TYPE);
    }

    @Override
    public HandlerRegistration addCancelAnalysisSelectedHandler(CancelAnalysisSelected.CancelAnalysisSelectedHandler handler) {
        return addHandler(handler, CancelAnalysisSelected.TYPE);
    }

    @Override
    public HandlerRegistration addViewAnalysisParamsSelectedHandler(ViewAnalysisParamsSelected.ViewAnalysisParamsSelectedHandler handler) {
        return addHandler(handler, ViewAnalysisParamsSelected.TYPE);
    }
}
