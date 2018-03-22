package org.iplantc.de.client.services.impl;

import org.iplantc.de.client.models.UserInfo;
import org.iplantc.de.client.models.querydsl.QueryDSLTemplate;
import org.iplantc.de.client.models.sharing.PermissionValue;
import org.iplantc.de.client.util.SearchModelUtils;

import com.google.common.base.Strings;
import com.google.web.bindery.autobean.shared.Splittable;
import com.google.web.bindery.autobean.shared.impl.StringQuoter;

import java.util.List;
import java.util.logging.Logger;

/**
 *
 * This class uses a builder pattern to construct a search query from a given query template.
 *
 * If a field in the given query template is null or empty, the corresponding search term will be omitted
 * from the final query.
 *
 * @author aramsey
 */
@SuppressWarnings("nls")
public class DataSearchQueryBuilderv2 {

    // QUERY BUILDING BLOCKS
    public static final String ALL = "all";
    public static final String ANY = "any";
    public static final String NONE = "none";

    public static final String TYPE = "type";
    public static final String ARGS = "args";
    public static final String QUERY = "query";
    public static final String EXACT = "exact";


    // CLAUSES

    //Label
    public static final String LABEL = "label";
    //Path
    public static final String PATH = "path";
    public static final String PREFIX = "prefix";
    //Owner
    public static final String OWNER = "owner";
    //Permissions
    public static final String PERMISSIONS = "permissions";
    public static final String PERMISSION = "permission";
    public static final String PERMISSION_RECURSE = "permission_recurse";
    public static final String SHARED_WITH = "users";
    //Tag
    public static final String TAG = "tag";
    public static final String TAGS = "tags";
    //Metadata
    public static final String METADATA = "metadata";
    public static final String ATTRIBUTE = "attribute";
    public static final String ATTRIBUTE_EXACT = "attribute_exact";
    public static final String VALUE = "value";
    public static final String VALUE_EXACT = "value_exact";
    public static final String UNIT = "unit";
    public static final String UNIT_EXACT = "unit_exact";
    public static final String METADATA_TYPES = "metadata_types";


    private final QueryDSLTemplate template;
    private final UserInfo userinfo;
    private final Splittable allList;
    private final Splittable anyList;
    private final Splittable noneList;
    private final SearchModelUtils searchModelUtils;

    Splittable query = StringQuoter.createSplittable();
    Splittable all = addChild(query, QUERY);

    Logger LOG = Logger.getLogger(DataSearchQueryBuilderv2.class.getName());

    public DataSearchQueryBuilderv2(QueryDSLTemplate template, UserInfo userinfo) {
        this.template = template;
        this.userinfo = userinfo;
        this.searchModelUtils = SearchModelUtils.getInstance();
        allList = StringQuoter.createIndexed();
        anyList = StringQuoter.createIndexed();
        noneList = StringQuoter.createIndexed();
    }

    public String buildFullQuery() {
        ownedBy()
                .pathPrefix()
                .sharedWith()
                .file();

        LOG.fine("search query==>" + toString());
        return toString();
    }

    /**
     * {"type": "owner", "args": {"owner": "ipcdev"}}
     */
    public DataSearchQueryBuilderv2 ownedBy() {
        String queryContent = template.getOwnedBy();
        if (!Strings.isNullOrEmpty(queryContent)) {
            appendArrayItem(allList, createTypeClause(OWNER, OWNER, queryContent));
        }
        return this;
    }

    /**
     * {"type": "label", "args": {"label": "some_random_file_name", "exact": true}}
     */
    public DataSearchQueryBuilderv2 file() {
        String content = template.getNameHas();
        if (!Strings.isNullOrEmpty(content)) {
            Splittable args = StringQuoter.createSplittable();
            assignKeyValue(args, LABEL, content);
            assignKeyValue(args, EXACT, template.isLabelExact());
            appendArrayItem(allList, createTypeClause(LABEL, args));
        }
        return this;
    }

    public DataSearchQueryBuilderv2 sharedWith() {
        List<String> content = template.getPermissionUsers();
        if (content != null && !content.isEmpty()) {
            Splittable users = listToSplittable(content);
            Splittable args = StringQuoter.createSplittable();
            assignKeyValue(args, SHARED_WITH, users);
            if (Strings.isNullOrEmpty(template.getPermission())) {
                template.setPermission(PermissionValue.read.toString());
                template.setPermissionRecurse(true);
            }
            assignKeyValue(args, PERMISSION, template.getPermission());
            assignKeyValue(args, PERMISSION_RECURSE, template.isPermissionRecurse());
            appendArrayItem(allList, createTypeClause(PERMISSIONS, args));
        }
        return this;
    }

    /**
     * {"type": "path", "args": {"prefix": "/iplant/home/userName"}}
     */
    public DataSearchQueryBuilderv2 pathPrefix() {
        String queryContent = template.getPathPrefix();
        if (!Strings.isNullOrEmpty(queryContent)) {
            appendArrayItem(allList, createTypeClause(PATH, PREFIX, queryContent));
        }
        return this;
    }

    /**
     * {"type": "typeVal", "args": {"argKey": "argVal"}}
     */
    public Splittable createTypeClause(String typeVal, String argKey, String argVal) {
        Splittable argKeySpl = StringQuoter.createSplittable();
        assignKeyValue(argKeySpl, argKey, argVal);
        return createTypeClause(typeVal, argKeySpl);
    }

    /**
     * {"type": "typeVal", "args": {"argKey": "argVal"}}
     */
    public Splittable createTypeClause(String typeVal, String argKey, Boolean argVal) {
        Splittable argKeySpl = StringQuoter.createSplittable();
        assignKeyValue(argKeySpl, argKey, argVal);
        return createTypeClause(typeVal, argKeySpl);
    }

    /**
     * {"type": "typeVal", "args": {"argKey": argVal}}
     */
    public Splittable createTypeClause(String typeVal, String argKey, Splittable argVal) {
        Splittable argKeySpl = StringQuoter.createSplittable();
        assignKeyValue(argKeySpl, argKey, argVal);
        return createTypeClause(typeVal, argKeySpl);
    }

    /**
     * {"type": "typeVal", "args": {"argKey": "argVal"}}
     */
    public Splittable createTypeClause(String typeVal, Splittable args) {
        Splittable type = StringQuoter.createSplittable();

        assignKeyValue(type, TYPE, typeVal);
        assignKeyValue(type, ARGS, args);

        return type;
    }

    public Splittable listToSplittable(List<String> list) {
        Splittable splittable = StringQuoter.createIndexed();
        list.forEach(value -> StringQuoter.create(value).assign(splittable, splittable.size()));
        return splittable;
    }

    /**
     * Takes a splittable, assigns the specified key and value
     * { "key" : "value" }
     * @param keySplittable
     * @param key
     * @param value
     */
    public void assignKeyValue(Splittable keySplittable, String key, String value) {
        StringQuoter.create(value).assign(keySplittable, key);
    }

    /**
     * Takes a splittable, assigns the specified key and value
     * { "key" : { "someKey" : "someValue" } }
     * @param keySplittable
     * @param key
     * @param value
     */
    public void assignKeyValue(Splittable keySplittable, String key, Splittable value) {
        value.assign(keySplittable, key);
    }

    /**
     * Takes a splittable, assigns the specified key and boolean value
     * { "key" : true }
     * @param keySplittable
     * @param key
     * @param value
     */
    public void assignKeyValue(Splittable keySplittable, String key, boolean value) {
        StringQuoter.create(value).assign(keySplittable, key);
    }

    private Splittable addChild(Splittable parent, String key) {
        Splittable child = StringQuoter.createSplittable();
        child.assign(parent, key);
        return child;
    }

    /**
     * Takes a splittable array and adds the specified item to it
     * @param array
     * @param item
     */
    private void appendArrayItem(Splittable array, Splittable item) {
        item.assign(array, array.size());
    }

    /**
     * @return the currently constructed query.
     */
    @Override
    public String toString() {
        // {"all":[{"type": "randomValue"}, {"type": "otherRandomValue"}]}

        allList.assign(all, ALL);

        return query.getPayload();
    }
}
