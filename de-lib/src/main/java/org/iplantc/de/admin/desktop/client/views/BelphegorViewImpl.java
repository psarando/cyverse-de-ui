package org.iplantc.de.admin.desktop.client.views;

import org.iplantc.de.admin.apps.client.AdminAppStatsGridView;
import org.iplantc.de.admin.apps.client.AppPublicationRequestView;
import org.iplantc.de.admin.desktop.client.communities.AdminCommunitiesView;
import org.iplantc.de.admin.desktop.client.metadata.view.TemplateListingView;
import org.iplantc.de.admin.desktop.client.ontologies.OntologiesView;
import org.iplantc.de.admin.desktop.client.permIdRequest.views.PermanentIdRequestView;
import org.iplantc.de.admin.desktop.client.refGenome.RefGenomeView;
import org.iplantc.de.admin.desktop.client.toolAdmin.ToolAdminView;
import org.iplantc.de.admin.desktop.client.toolRequest.ToolRequestView;
import org.iplantc.de.admin.desktop.client.workshopAdmin.WorkshopAdminView;
import org.iplantc.de.admin.desktop.shared.Belphegor;
import org.iplantc.de.client.models.UserInfo;
import org.iplantc.de.commons.client.widgets.DETabPanel;
import org.iplantc.de.commons.client.widgets.IPlantAnchor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiFactory;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.AbstractHtmlLayoutContainer.HtmlData;
import com.sencha.gxt.widget.core.client.container.HtmlLayoutContainer;
import com.sencha.gxt.widget.core.client.container.SimpleContainer;
import com.sencha.gxt.widget.core.client.menu.Menu;

/**
 * @author jstroot
 */
public class BelphegorViewImpl extends Composite implements BelphegorView {

    private static BelphegorViewUiBinder uiBinder = GWT.create(BelphegorViewUiBinder.class);

    @UiTemplate("BelphegorView.ui.xml")
    interface BelphegorViewUiBinder extends UiBinder<Widget, BelphegorViewImpl> {}

    @UiField HtmlLayoutContainer northCon;
    @UiField DETabPanel deTabPanel;
    @UiField SimpleContainer ontologiesPanel, communitiesPanel, refGenomePanel, toolRequestPanel, metadataPanel,
            permIdPanel, toolAdminPanel, workshopAdminPanel, statsPanel, appPubRequestPanel;
    @UiField(provided = true) BelphegorViewAppearance appearance;
    private TextButton menuButton;
    private RefGenomeView.Presenter refGenPresenter;
    private ToolRequestView.Presenter toolReqPresenter;
    private ToolAdminView.Presenter toolAdminPresenter;
    private TemplateListingView.Presenter tempPresenter;
    private PermanentIdRequestView.Presenter permIdPresenter;
    private OntologiesView.Presenter ontologiesPresenter;
    private WorkshopAdminView.Presenter workshopAdminPresenter;
    private AdminAppStatsGridView.Presenter appStatsPresenter;
    private AdminCommunitiesView.Presenter communitiesPresenter;
    private AppPublicationRequestView.Presenter appPublicationRequestsPresenter;


    @Inject
    public BelphegorViewImpl(final OntologiesView.Presenter ontologiesPresenter,
                             final AdminCommunitiesView.Presenter communitiesPresenter,
                             final RefGenomeView.Presenter refGenPresenter,
                             final ToolRequestView.Presenter toolReqPresenter,
                             final ToolAdminView.Presenter toolAdminPresenter,
                             final TemplateListingView.Presenter tempPresenter,
                             final PermanentIdRequestView.Presenter permIdPresenter,
                             final WorkshopAdminView.Presenter workshopAdminPresenter,
                             final AdminAppStatsGridView.Presenter appStatsPresenter,
                             final AppPublicationRequestView.Presenter appPublicationRequestsPresenter,
                             final BelphegorViewAppearance appearance) {
        this.communitiesPresenter = communitiesPresenter;
        this.appearance = appearance;
        this.ontologiesPresenter = ontologiesPresenter;
        this.refGenPresenter = refGenPresenter;
        this.toolReqPresenter = toolReqPresenter;
        this.toolAdminPresenter = toolAdminPresenter;
        this.tempPresenter = tempPresenter;
        this.permIdPresenter = permIdPresenter;
        this.workshopAdminPresenter = workshopAdminPresenter;
        this.appStatsPresenter = appStatsPresenter;
        this.appPublicationRequestsPresenter = appPublicationRequestsPresenter;

        initWidget(uiBinder.createAndBindUi(this));
        init(ontologiesPresenter,
             communitiesPresenter,
             refGenPresenter,
             toolReqPresenter,
             toolAdminPresenter,
             tempPresenter,
             permIdPresenter,
             workshopAdminPresenter,
             appStatsPresenter,
             appPublicationRequestsPresenter);
        ensureDebugId(Belphegor.Ids.BELPHEGOR);
    }

    @UiFactory
    HtmlLayoutContainer buildHtmlLayoutContainer() {
        return new HtmlLayoutContainer(appearance.renderNorthContainer());
    }

    private void init(final OntologiesView.Presenter ontologiesPresenter,
                      final AdminCommunitiesView.Presenter communitiesPresenter,
                      final RefGenomeView.Presenter refGenPresenter,
                      final ToolRequestView.Presenter toolReqPresenter,
                      final ToolAdminView.Presenter toolAdminPresenter,
                      final TemplateListingView.Presenter tempPresenter,
                      final PermanentIdRequestView.Presenter permIdPresenter,
                      final WorkshopAdminView.Presenter workshopAdminPresenter,
                      final AdminAppStatsGridView.Presenter appStatsPresenter,
                      final AppPublicationRequestView.Presenter appPublicationRequestsPresenter) {
        buildUserMenu();

        ontologiesPresenter.go(ontologiesPanel);
        communitiesPresenter.go(communitiesPanel);
        refGenPresenter.go(refGenomePanel);
        toolReqPresenter.go(toolRequestPanel);
        toolAdminPresenter.go(toolAdminPanel);
        tempPresenter.go(metadataPanel);
        permIdPresenter.go(permIdPanel);
        workshopAdminPresenter.go(workshopAdminPanel);
        appStatsPresenter.go(statsPanel);
        appPublicationRequestsPresenter.go(appPubRequestPanel);
    }

    private void buildUserMenu() {

        Menu userMenu = new Menu();

        userMenu.setBorders(true);
        userMenu.setStyleName(appearance.style().headerMenuBody());
        userMenu.add(new IPlantAnchor(appearance.logout(), -1, new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                doLogout();
            }
        }));

        String username = UserInfo.getInstance().getUsername();
        String firstName = UserInfo.getInstance().getFirstName();
        String lastName = UserInfo.getInstance().getLastName();
        String menuLabel = (firstName != null && lastName != null) ? firstName + " " + lastName : username;
        menuButton = new TextButton(menuLabel);
        menuButton.setMenu(userMenu);

        northCon.add(menuButton, new HtmlData("." + appearance.style().headerMenu()));
    }

    @Override
    protected void onEnsureDebugId(String baseID) {
        super.onEnsureDebugId(baseID);

        menuButton.ensureDebugId(baseID + Belphegor.Ids.MENU_BUTTON);

        deTabPanel.setTabDebugId(ontologiesPanel, baseID + Belphegor.Ids.CATALOG_TAB);
        ontologiesPanel.ensureDebugId(baseID + Belphegor.Ids.CATALOG);
        ontologiesPresenter.setViewDebugId(baseID + Belphegor.Ids.CATALOG);

        deTabPanel.setTabDebugId(communitiesPanel, baseID + Belphegor.Ids.COMMUNITIES_TAB);
        communitiesPanel.ensureDebugId(baseID + Belphegor.Ids.COMMUNITIES);
        communitiesPresenter.setViewDebugId(baseID + Belphegor.Ids.COMMUNITIES);

        deTabPanel.setTabDebugId(appPubRequestPanel, baseID + Belphegor.Ids.APP_PUBLICATION_REQUEST_TAB);
        appPubRequestPanel.ensureDebugId(baseID + Belphegor.Ids.APP_PUBLICATION_REQUEST);
        appPublicationRequestsPresenter.setViewDebugId(baseID + Belphegor.Ids.APP_PUBLICATION_REQUEST);

        deTabPanel.setTabDebugId(statsPanel, baseID + Belphegor.Ids.STATS_TAB);
        statsPanel.ensureDebugId(baseID + Belphegor.Ids.STATS);
        appStatsPresenter.setViewDebugId(baseID + Belphegor.Ids.STATS);

        deTabPanel.setTabDebugId(refGenomePanel, baseID + Belphegor.Ids.REFERENCE_GENOME_TAB);
        refGenomePanel.ensureDebugId(baseID + Belphegor.Ids.REFERENCE_GENOME);
        refGenPresenter.setViewDebugId(baseID + Belphegor.Ids.REFERENCE_GENOME);

        deTabPanel.setTabDebugId(toolRequestPanel, baseID + Belphegor.Ids.TOOL_REQUEST_TAB);
        toolRequestPanel.ensureDebugId(baseID + Belphegor.Ids.TOOL_REQUEST);
        toolReqPresenter.setViewDebugId(baseID + Belphegor.Ids.TOOL_REQUEST);

        deTabPanel.setTabDebugId(toolAdminPanel, baseID + Belphegor.Ids.TOOL_ADMIN_TAB);
        toolAdminPanel.ensureDebugId(baseID + Belphegor.Ids.TOOL_ADMIN);
        toolAdminPresenter.setViewDebugId(baseID + Belphegor.Ids.TOOL_ADMIN);

        deTabPanel.setTabDebugId(metadataPanel, baseID + Belphegor.Ids.METADATA_TAB);
        metadataPanel.ensureDebugId(baseID + Belphegor.Ids.METADATA);
        tempPresenter.setViewDebugId(baseID + Belphegor.Ids.METADATA);

        deTabPanel.setTabDebugId(permIdPanel, baseID + Belphegor.Ids.PERMID_TAB);
        permIdPanel.ensureDebugId(baseID + Belphegor.Ids.PERMID);
        permIdPresenter.setViewDebugId(baseID + Belphegor.Ids.PERMID);

        deTabPanel.setTabDebugId(workshopAdminPanel, baseID + Belphegor.Ids.WORKSHOP_ADMIN_TAB);
        workshopAdminPanel.ensureDebugId(baseID + Belphegor.Ids.WORKSHOP_ADMIN);
        workshopAdminPresenter.setViewDebugId(baseID + Belphegor.Ids.WORKSHOP_ADMIN);
    }

    @Override
    public void doLogout() {
        Window.Location.assign(appearance.logoutWindowUrl());
    }
}
