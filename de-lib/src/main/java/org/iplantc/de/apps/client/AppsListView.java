package org.iplantc.de.apps.client;

import org.iplantc.de.apps.client.events.AppSearchResultLoadEvent;
import org.iplantc.de.apps.client.events.BeforeAppSearchEvent;
import org.iplantc.de.apps.client.events.SwapViewButtonClickedEvent;
import org.iplantc.de.apps.client.events.selection.AppCategorySelectionChangedEvent;
import org.iplantc.de.apps.client.events.selection.AppInfoSelectedEvent;
import org.iplantc.de.apps.client.events.selection.AppSelectionChangedEvent;
import org.iplantc.de.apps.client.events.selection.CommunitySelectionChangedEvent;
import org.iplantc.de.apps.client.events.selection.DeleteAppsSelected;
import org.iplantc.de.apps.client.events.selection.OntologyHierarchySelectionChangedEvent;
import org.iplantc.de.apps.client.events.selection.RunAppSelected;
import org.iplantc.de.client.models.apps.App;
import org.iplantc.de.client.models.apps.AppCategory;

import com.google.gwt.user.client.ui.IsWidget;
import com.google.web.bindery.autobean.shared.Splittable;

import jsinterop.annotations.JsIgnore;
import jsinterop.annotations.JsType;

/**
 * @author aramsey
 */
@JsType
public interface AppsListView extends IsWidget/*   ,
                                      IsMaskable,
                                      AppSelectionChangedEvent.HasAppSelectionChangedEventHandlers,
                                      AppInfoSelectedEvent.HasAppInfoSelectedEventHandlers,
                                      AppNameSelectedEvent.HasAppNameSelectedEventHandlers,
                                      AppFavoriteSelectedEvent.HasAppFavoriteSelectedEventHandlers,
                                      AppCommentSelectedEvent.HasAppCommentSelectedEventHandlers,
                                      AppRatingSelected.HasAppRatingSelectedEventHandlers,
                                      AppRatingDeselected.HasAppRatingDeselectedHandlers,
                                      AppSearchResultLoadEvent.AppSearchResultLoadEventHandler,
                                      AppCategorySelectionChangedEvent.AppCategorySelectionChangedEventHandler,
                                      AppFavoritedEvent.HasAppFavoritedEventHandlers,
                                      BeforeAppSearchEvent.BeforeAppSearchEventHandler,
                                      OntologyHierarchySelectionChangedEvent.OntologyHierarchySelectionChangedEventHandler,
                                      CommunitySelectionChangedEvent.CommunitySelectionChangedEventHandler*/ {
    String GRID_VIEW = "grid";
    String TILE_VIEW = "tile";

    interface AppsListAppearance {

        String appLaunchWithoutToolError();

        String appRemoveFailure();

        String beforeAppSearchLoadingMask();

        String favServiceFailure();

        String getAppsLoadingMask();

        String integratedByColumnLabel();

        String nameColumnLabel();

        String ratingColumnLabel();

        String searchAppResultsHeader(String searchText, int total);

        String agaveAuthRequiredTitle();

        String agaveAuthRequiredMsg();

        String sortLabel();

        String appLoadError();

        String noApps();

        int executionSystemWidth();

        String executionSystemLabel();

        int menuColumnWidth();
    }

    /**
     * This presenter is responsible for updating/maintaining the {@code ListStore} associated with the
     * view. It fires store related events for other presenters. \
     *
     * To update the {@code ListStore}, it listens for {@link AppCategory} selection and search result
     * load events.
     */
    @JsType
    interface Presenter extends org.iplantc.de.commons.client.presenter.Presenter,
                                AppCategorySelectionChangedEvent.AppCategorySelectionChangedEventHandler,
                                AppSearchResultLoadEvent.AppSearchResultLoadEventHandler,
                                DeleteAppsSelected.DeleteAppsSelectedHandler,
                                RunAppSelected.RunAppSelectedHandler,
                                BeforeAppSearchEvent.BeforeAppSearchEventHandler,
                                OntologyHierarchySelectionChangedEvent.OntologyHierarchySelectionChangedEventHandler,
                                SwapViewButtonClickedEvent.SwapViewButtonClickedEventHandler,
                                AppSelectionChangedEvent.HasAppSelectionChangedEventHandlers,
                                AppInfoSelectedEvent.HasAppInfoSelectedEventHandlers,
                                CommunitySelectionChangedEvent.CommunitySelectionChangedEventHandler {
        App getSelectedApp();

        @JsIgnore
        void setViewDebugId(String baseID);

        @JsIgnore
        String getActiveView();

        @JsIgnore
        void setActiveView(String activeView);

        @JsIgnore
        void loadApps(Splittable apps);

        void onTypeFilterChanged(String filter);

        void onAppSelectionChanged(Splittable selectedApps);

        void onAppNameSelected(Splittable appSplittable);

        void onAppInfoSelected(Splittable appSplittable);

        void onAppFavoriteSelected(Splittable appSplittable);

        void onAppCommentSelected(Splittable appSplittable);

        void onRequestSort(String sortField);

        void onAppRatingDeselected(final Splittable appSplittable);

        void onAppRatingSelected(final Splittable appSplittable,
                                 int score);
    }

    void load(AppsListView.Presenter presenter,
              String activeView,
              String baseId);

    void disableTypeFilter(boolean disable);

    void setSearchRegexPattern(String pattern);

    void setHeading(String heading);

    void setLoadingMask(boolean loading);

    void setApps(Splittable apps, boolean loading);

    void render();

    void setViewType(String viewType);

    void loadSearchResults(Splittable apps,
                           String searchRegexPattern,
                           String heading,
                           boolean loading);


    void setTypeFilter(String filter);

    void setSortField(String sortField);

}
