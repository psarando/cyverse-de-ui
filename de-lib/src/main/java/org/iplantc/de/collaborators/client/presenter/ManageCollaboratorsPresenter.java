/**
 * 
 */
package org.iplantc.de.collaborators.client.presenter;

import org.iplantc.de.client.events.EventBus;
import org.iplantc.de.client.models.UserInfo;
import org.iplantc.de.client.models.collaborators.Collaborator;
import org.iplantc.de.client.models.groups.Group;
import org.iplantc.de.client.services.GroupServiceFacade;
import org.iplantc.de.collaborators.client.events.RemoveCollaboratorSelected;
import org.iplantc.de.collaborators.client.events.UserSearchResultSelected;
import org.iplantc.de.collaborators.client.gin.ManageCollaboratorsViewFactory;
import org.iplantc.de.collaborators.client.util.CollaboratorsUtil;
import org.iplantc.de.collaborators.client.views.ManageCollaboratorsView;
import org.iplantc.de.commons.client.ErrorHandler;
import org.iplantc.de.commons.client.info.ErrorAnnouncementConfig;
import org.iplantc.de.commons.client.info.IplantAnnouncer;
import org.iplantc.de.resources.client.messages.I18N;

import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HasOneWidget;
import com.google.inject.Inject;

import java.util.Arrays;
import java.util.List;

/**
 * @author sriram
 * 
 */
public class ManageCollaboratorsPresenter implements ManageCollaboratorsView.Presenter,
                                                     RemoveCollaboratorSelected.RemoveCollaboratorSelectedHandler{

    private ManageCollaboratorsView view;
    private HandlerRegistration addCollabHandlerRegistration;

    private final class UserSearchResultSelectedEventHandlerImpl implements
                                                                 UserSearchResultSelected.UserSearchResultSelectedEventHandler {
        @Override
           public void
                   onUserSearchResultSelected(UserSearchResultSelected userSearchResultSelected) {
               if (userSearchResultSelected.getTag()
                                           .equalsIgnoreCase(UserSearchResultSelected.USER_SEARCH_EVENT_TAG.MANAGE.toString())) {
                   Collaborator collaborator = userSearchResultSelected.getCollaborator();
                   if (!UserInfo.getInstance()
                                .getUsername()
                                .equals(collaborator.getUserName())) {
                       if (!collaboratorsUtil.isCurrentCollaborator(collaborator)) {
                           addAsCollaborators(Arrays.asList(collaborator));
                       }
                   } else {
                       IplantAnnouncer.getInstance()
                                      .schedule(new ErrorAnnouncementConfig(I18N.DISPLAY.collaboratorSelfAdd()));
                   }
               }
           }
    }

    @Inject CollaboratorsUtil collaboratorsUtil;
    private ManageCollaboratorsViewFactory factory;
    private GroupServiceFacade groupServiceFacade;

    @Inject
    public ManageCollaboratorsPresenter(ManageCollaboratorsViewFactory factory,
                                        GroupServiceFacade groupServiceFacade) {
        this.factory = factory;
        this.groupServiceFacade = groupServiceFacade;
    }

    private void addEventHandlers() {
        addCollabHandlerRegistration = EventBus.getInstance()
                                               .addHandler(UserSearchResultSelected.TYPE,
                                                           new UserSearchResultSelectedEventHandlerImpl());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.iplantc.de.commons.client.presenter.Presenter#go(com.google.gwt.user.client.ui.HasOneWidget )
     */
    @Override
    public void go(HasOneWidget container, ManageCollaboratorsView.MODE mode) {
        this.view = factory.create(mode);
        view.addRemoveCollaboratorSelectedHandler(this);
        loadCurrentCollaborators();
        updateListView();
        addEventHandlers();
        container.setWidget(view.asWidget());
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.iplantc.de.client.collaborators.views.ManageCollaboratorsView.Presenter#addAsCollaborators
     * (java.util.List)
     */
    @Override
    public void addAsCollaborators(final List<Collaborator> models) {
        collaboratorsUtil.addCollaborators(models, new AsyncCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
                // remove added models from search results
                view.addCollaborators(models);
            }

            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(caught);
            }
        });

    }

    void updateListView() {
        String searchTerm = "*";
        updateListView(searchTerm);
    }

    @Override
    public void updateListView(String searchTerm) {
        groupServiceFacade.getGroups(searchTerm, new AsyncCallback<List<Group>>() {
            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(caught);
            }

            @Override
            public void onSuccess(List<Group> result) {
                view.addCollabLists(result);
            }
        });
    }

    @Override
    public void onRemoveCollaboratorSelected(RemoveCollaboratorSelected event) {
        List<Collaborator> models = event.getCollaborators();
        collaboratorsUtil.removeCollaborators(models, new AsyncCallback<Void>() {

            @Override
            public void onSuccess(Void result) {
                view.removeCollaborators(models);

            }

            @Override
            public void onFailure(Throwable caught) {
                ErrorHandler.post(caught);
            }
        });

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.iplantc.de.client.collaborators.views.ManageCollaboratorsView.Presenter#loadCurrentCollaborators
     * ()
     */
    @Override
    public void loadCurrentCollaborators() {
        view.mask(null);
        collaboratorsUtil.getCollaborators(new AsyncCallback<Void>() {

            @Override
            public void onFailure(Throwable caught) {
                view.unmask();
            }

            @Override
            public void onSuccess(Void result) {
                view.unmask();
                view.loadData(collaboratorsUtil.getCurrentCollaborators());
            }

        });

    }

    @Override
    public void setCurrentMode(ManageCollaboratorsView.MODE m) {
        view.setMode(m);
    }

    @Override
    public ManageCollaboratorsView.MODE getCurrentMode() {
        return view.getMode();
    }

    @Override
    public List<Collaborator> getSelectedCollaborators() {
        return view.getSelectedCollaborators();
    }

    @Override
    public void cleanup() {
        if (addCollabHandlerRegistration != null) {
            addCollabHandlerRegistration.removeHandler();
        }
    }

}
