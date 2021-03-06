package org.iplantc.de.diskResource.client.views.search;

import org.iplantc.de.collaborators.client.util.CollaboratorsUtil;
import org.iplantc.de.diskResource.client.SearchView;

import com.google.web.bindery.autobean.shared.Splittable;

import gwt.react.client.components.ComponentConstructorFn;
import gwt.react.client.proptypes.BaseProps;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsProperty;
import jsinterop.annotations.JsType;

@JsType(isNative = true, namespace = "CyVerseReactComponents.data.search", name = "QueryBuilder")
public class ReactSearchForm {

    @JsProperty(namespace = "CyVerseReactComponents.data.search", name = "QueryBuilder")
    public static ComponentConstructorFn<SearchFormProps> SearchForm;

    @JsType(isNative = true, namespace = JsPackage.GLOBAL, name = "Object")
    public static class SearchFormProps extends BaseProps {
        public SearchView.Presenter presenter;
        public String parentId;
        public Splittable initialValues;
        public CollaboratorsUtil collaboratorsUtil;
    }
}
