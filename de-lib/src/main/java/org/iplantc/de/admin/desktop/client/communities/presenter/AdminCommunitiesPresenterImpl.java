package org.iplantc.de.admin.desktop.client.communities.presenter;

import org.iplantc.de.admin.apps.client.AdminAppsGridView;
import org.iplantc.de.admin.desktop.client.communities.AdminCommunitiesView;
import org.iplantc.de.admin.desktop.client.communities.events.CommunitySelectionChanged;
import org.iplantc.de.admin.desktop.client.communities.gin.AdminCommunitiesViewFactory;
import org.iplantc.de.admin.desktop.client.communities.service.AdminCommunityServiceFacade;
import org.iplantc.de.admin.desktop.client.ontologies.events.HierarchySelectedEvent;
import org.iplantc.de.admin.desktop.client.ontologies.service.OntologyServiceFacade;
import org.iplantc.de.admin.desktop.client.services.AppAdminServiceFacade;
import org.iplantc.de.admin.desktop.shared.Belphegor;
import org.iplantc.de.apps.client.events.AppSearchResultLoadEvent;
import org.iplantc.de.apps.client.presenter.toolBar.proxy.AppSearchRpcProxy;
import org.iplantc.de.client.DEClientConstants;
import org.iplantc.de.client.models.apps.App;
import org.iplantc.de.client.models.groups.Group;
import org.iplantc.de.client.models.ontologies.Ontology;
import org.iplantc.de.client.models.ontologies.OntologyHierarchy;
import org.iplantc.de.client.services.AppSearchFacade;
import org.iplantc.de.client.services.AppServiceFacade;
import org.iplantc.de.client.util.JsonUtil;
import org.iplantc.de.client.util.OntologyUtil;
import org.iplantc.de.commons.client.ErrorHandler;
import org.iplantc.de.commons.client.info.IplantAnnouncer;
import org.iplantc.de.shared.AppsCallback;
import org.iplantc.de.shared.DEProperties;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.inject.Inject;

import com.sencha.gxt.data.shared.TreeStore;
import com.sencha.gxt.data.shared.loader.FilterPagingLoadConfig;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;

import java.util.List;

/**
 * @author aramsey
 */
public class AdminCommunitiesPresenterImpl implements AdminCommunitiesView.Presenter,
                                                      AppSearchResultLoadEvent.AppSearchResultLoadEventHandler {

    @Inject AppAdminServiceFacade adminAppService;
    @Inject DEClientConstants constants;
    @Inject DEProperties properties;
    @Inject IplantAnnouncer announcer;
    @Inject JsonUtil jsonUtil;
    @Inject AppServiceFacade appService;
    private OntologyServiceFacade ontologyServiceFacade;
    @Inject AppSearchFacade appSearchService;
    OntologyUtil ontologyUtil;
    AppSearchRpcProxy proxy;
    PagingLoader<FilterPagingLoadConfig, PagingLoadResult<App>> loader;
    private AdminCommunitiesView view;
    private AdminCommunityServiceFacade serviceFacade;
    private final TreeStore<Group> communityTreeStore;
    private final TreeStore<OntologyHierarchy> hierarchyTreeStore;
    private AdminCommunitiesView.Appearance appearance;
    private AdminAppsGridView.Presenter hierarchyGridPresenter;
    private AdminAppsGridView.Presenter communityGridPresenter;
    String activeOntologyVersion;

    @Inject
    public AdminCommunitiesPresenterImpl(AdminCommunityServiceFacade serviceFacade,
                                         OntologyServiceFacade ontologyServiceFacade,
                                         AppSearchFacade appSearchService,
                                         AppServiceFacade appService,
                                         final TreeStore<Group> communityTreeStore,
                                         final TreeStore<OntologyHierarchy> hierarchyTreeStore,
                                         AdminCommunitiesViewFactory factory,
                                         AdminCommunitiesView.Appearance appearance,
                                         AdminAppsGridView.Presenter hierarchyGridPresenter,
                                         AdminAppsGridView.Presenter communityGridPresenter) {
        this.serviceFacade = serviceFacade;
        this.ontologyServiceFacade = ontologyServiceFacade;
        this.appSearchService = appSearchService;
        this.communityTreeStore = communityTreeStore;
        this.hierarchyTreeStore = hierarchyTreeStore;
        this.appearance = appearance;
        this.ontologyUtil = OntologyUtil.getInstance();

        this.hierarchyGridPresenter = hierarchyGridPresenter;
        this.communityGridPresenter = communityGridPresenter;

        proxy = getProxy(appSearchService);
        loader = getPagingLoader();
        this.view = factory.create(communityTreeStore,
                                   hierarchyTreeStore,
                                   loader,
                                   hierarchyGridPresenter.getView(),
                                   communityGridPresenter.getView());

        hierarchyGridPresenter.getView().addAppSelectionChangedEventHandler(view);
        communityGridPresenter.getView().addAppSelectionChangedEventHandler(view);

        proxy.setHasHandlers(view);

        view.addCommunitySelectionChangedHandler(this);
        view.addHierarchySelectedEventHandler(this);
        view.addHierarchySelectedEventHandler(hierarchyGridPresenter.getView());
        view.addAppSearchResultLoadEventHandler(hierarchyGridPresenter);
        view.addAppSearchResultLoadEventHandler(hierarchyGridPresenter.getView());
        view.addBeforeAppSearchEventHandler(hierarchyGridPresenter.getView());
    }

    @Override
    public void setViewDebugId(String id) {
        view.asWidget().ensureDebugId(id + Belphegor.CommunityIds.VIEW);
    }

    PagingLoader<FilterPagingLoadConfig, PagingLoadResult<App>> getPagingLoader() {
        return new PagingLoader<>(proxy);
    }

    AppSearchRpcProxy getProxy(AppSearchFacade appService) {
        return new AppSearchRpcProxy(appService);
    }

    @Override
    public void go(HasOneWidget container) {
        getCommunities();
        getHierarchies();
        container.setWidget(view);
    }

    @Override
    public AdminCommunitiesView getView() {
        return view;
    }

    boolean previewTreeHasHierarchy(OntologyHierarchy hierarchy) {
        String id = ontologyUtil.getOrCreateHierarchyPathTag(hierarchy);
        return hierarchyTreeStore.findModelWithKey(id) != null;
    }

    void getHierarchies() {
        ontologyServiceFacade.getOntologies(new AsyncCallback<List<Ontology>>() {
            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(caught);
            }

            @Override
            public void onSuccess(List<Ontology> result) {
                if (result != null) {
                    Ontology activeOntology = result.stream().filter(Ontology::isActive).findFirst().get();
                    activeOntologyVersion = activeOntology.getVersion();
                    ontologyServiceFacade.getOntologyHierarchies(activeOntologyVersion, new AppsCallback<List<OntologyHierarchy>>() {
                        @Override
                        public void onFailure(Integer statusCode, Throwable exception) {
                            ErrorHandler.post(exception);
                        }

                        @Override
                        public void onSuccess(List<OntologyHierarchy> result) {
                            addHierarchies(null, result);
                        }
                    });
                }
            }
        });
    }

    void addHierarchies(OntologyHierarchy parent,
                        List<OntologyHierarchy> children) {
        if ((children == null)
            || children.isEmpty()) {
            return;
        }
        if (parent == null) {
            ontologyUtil.addUnclassifiedChild(children);
            hierarchyTreeStore.add(children);
        } else {
            hierarchyTreeStore.add(parent, children);
        }

        for (OntologyHierarchy hierarchy : children) {
            addHierarchies(hierarchy, hierarchy.getSubclasses());
        }
    }

    void getCommunities() {
        communityGridPresenter.getView().clearAndAdd(null);
        hierarchyGridPresenter.getView().clearAndAdd(null);

        serviceFacade.getCommunities(new AsyncCallback<List<Group>>() {
            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(caught);
            }

            @Override
            public void onSuccess(List<Group> result) {
                if (!result.isEmpty()) {
                    communityTreeStore.add(result);
                    view.showCommunitiesPanel();
                } else {
                    view.showNoCommunitiesPanel();
                }
            }
        });
    }

    @Override
    public Group getCommunityFromElement(Element el) {
        return view.getCommunityFromElement(el);
    }

    @Override
    public Group getSelectedCommunity() {
        return view.getSelectedCommunity();
    }

    @Override
    public void onCommunitySelectionChanged(CommunitySelectionChanged event) {
        Group community = event.getCommunity();
        AdminAppsGridView communityApps = communityGridPresenter.getView();
        communityApps.mask(appearance.loadingMask());

        serviceFacade.getCommunityApps(community, new AppsCallback<List<App>>() {

            @Override
            public void onFailure(Integer statusCode, Throwable exception) {
                communityApps.unmask();
                ErrorHandler.post(exception);
            }

            @Override
            public void onSuccess(List<App> result) {
                communityApps.clearAndAdd(result);
                communityApps.unmask();
            }
        });
    }

    @Override
    public void onHierarchySelected(HierarchySelectedEvent event) {
        AdminAppsGridView hierarchyView = hierarchyGridPresenter.getView();
        hierarchyView.mask(appearance.loadingMask());
        OntologyHierarchy hierarchy = event.getHierarchy();
        ontologyServiceFacade.getAppsByHierarchy(activeOntologyVersion,
                                                 hierarchy.getIri(),
                                                 ontologyUtil.getAttr(hierarchy),
                                                 new AsyncCallback<List<App>>() {
                                                     @Override
                                                     public void onFailure(Throwable caught) {
                                                         ErrorHandler.post(caught);
                                                         hierarchyView.unmask();
                                                     }

                                                     @Override
                                                     public void onSuccess(List<App> result) {
                                                        hierarchyView.clearAndAdd(result);
                                                        hierarchyView.unmask();
                                                     }
                                                 });
    }

    @Override
    public void onAppSearchResultLoad(AppSearchResultLoadEvent event) {
        view.deselectHierarchies();
    }
}
