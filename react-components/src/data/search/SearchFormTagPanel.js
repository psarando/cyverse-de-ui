import EditTagDialog from "./EditTagDialog";
import messages from "./messages";
import TagPanel from "../details/TagPanel";
import withI18N from "../../util/I18NWrapper";

import PropTypes from "prop-types";
import React, { Component } from "react";

/**
 * @author aramsey
 * A wrapper around the TagPanel component that handles editing tags, specifically designed
 * for the SearchForm with redux
 */
class SearchFormTagPanel extends Component {
    constructor(props) {
        super(props);

        this.state = {
            openEditTagDlg: false,
            selectedTag: null
        };

        //Tags
        this.onTagClicked = this.onTagClicked.bind(this);
        this.fetchTagSuggestions = this.fetchTagSuggestions.bind(this);
        this.onTagSelected = this.onTagSelected.bind(this);
        this.appendTag = this.appendTag.bind(this);
        this.removeTag = this.removeTag.bind(this);
        this.saveTagDescription = this.saveTagDescription.bind(this);
        this.closeEditTagDlg = this.closeEditTagDlg.bind(this);
    }

    onTagClicked(tag) {
        this.setState({
            openEditTagDlg: true,
            selectedTag: tag
        })
    }

    saveTagDescription(tag) {
        this.props.presenter.onEditTagSelected(tag);
        this.setState({
            openEditTagDlg: false,
            selectedTag: null
        });
    }

    closeEditTagDlg() {
        this.setState({
            openEditTagDlg: false,
            selectedTag: null
        })
    }

    fetchTagSuggestions(search) {
        this.props.presenter.fetchTagSuggestions(search);
    }

    onTagSelected(tag) {
        let {
            array,
            taggedWith,
            presenter
        } = this.props;

        if (tag.id !== tag.value) {
            this.appendTag(tag, array, taggedWith);
        } else {
            presenter.onAddTagSelected(tag.value, (newTag) => this.appendTag(newTag, array, taggedWith));
        }
    }

    appendTag(tag) {
        let { array, taggedWith } = this.props;
        array.insert('tagQuery', 0, tag);
        taggedWith.input.onChange('');
    }

    removeTag(tag, index) {
        let { array } = this.props;
        array.remove('tagQuery', index)
    }

    render() {
        let {
            tagQuery,
            placeholder,
            parentId,
            dataSource
        } = this.props;
        let { selectedTag, openEditTagDlg } = this.state;
        return (
            <div>
                <TagPanel baseID={parentId}
                          placeholder={placeholder}
                          onTagClick={this.onTagClicked}
                          handleTagSearch={this.fetchTagSuggestions}
                          dataSource={dataSource}
                          handleRemoveClick={this.removeTag}
                          handleTagSelect={this.onTagSelected}
                          tags={tagQuery.input.value ? tagQuery.input.value : []}/>
                <EditTagDialog open={openEditTagDlg}
                               tag={selectedTag}
                               handleSave={this.saveTagDescription}
                               handleClose={this.closeEditTagDlg}/>
            </div>
        )
    }
}

SearchFormTagPanel.propTypes = {
    parentId: PropTypes.string.isRequired,
    placeholder: PropTypes.any.isRequired,
    presenter: PropTypes.shape({
        onAddTagSelected: PropTypes.func.isRequired,
        fetchTagSuggestions: PropTypes.func.isRequired,
        onEditTagSelected: PropTypes.func.isRequired
    }),
    dataSource: PropTypes.array.isRequired,
    array: PropTypes.object.isRequired,
    taggedWith: PropTypes.object.isRequired,
    tagQuery: PropTypes.object.isRequired
};

export default withI18N(SearchFormTagPanel, messages);