/**
 * Imported in AllStats.js. This function imports the files for all the tabs that are present under TabPanels
 */

import React from "react";
import PropTypes from "prop-types";
import { AppBar, Tabs, Tab, Typography, Box } from "@material-ui/core";
import JobsTab from "./JobsTab";
import AppsTab from "./AppsTab";
import UsersTab from "./UsersTab";
import ids from "./AllStatsIDs";
import { getMessage, withI18N, build } from "@cyverse-de/ui-lib";
import myMessagesFile from "./messages.js";

function TabPanel(props) {
    const { children, value, baseId, index, ...other } = props;

    return (
        <Typography
            component="div"
            role="tabpanel"
            hidden={value !== index}
            id={`nav-tabpanel-${index}`}
            aria-labelledby={`nav-tab-${index}`}
            {...other}
        >
            <Box p={3}>{children}</Box>
        </Typography>
    );
}

TabPanel.propTypes = {
    children: PropTypes.node,
    index: PropTypes.any.isRequired,
    value: PropTypes.any.isRequired,
};
//
// function a11yProps(index) {
//     return {
//         id: `nav-tab-${index}`,
//         "aria-controls": `nav-tabpanel-${index}`,
//     };
// }

function NavTabs(props) {
    const classes = props;
    const [value, setValue] = React.useState(0);

    const handleChange = (event, newValue) => {
        setValue(newValue);
    };

    function a11yProps(index) {
        return {
            id: build(baseId, index),
        };
    }

    const { baseId } = props;
    return (
        <div className={classes.root} id={baseId}>
            <AppBar position="static">
                <Tabs variant="fullWidth" value={value} onChange={handleChange}>
                    <Tab
                        label={getMessage("jobsAndLogins")}
                        {...a11yProps(ids.JOBS_TAB)}
                    />
                    <Tab
                        label={getMessage("apps")}
                        {...a11yProps(ids.APPS_TAB)}
                    />
                    <Tab
                        label={getMessage("users")}
                        {...a11yProps(ids.USERS_TAB)}
                    />
                </Tabs>
            </AppBar>
            <TabPanel value={value} index={0}>
                <JobsTab baseId={build(baseId, ids.JOBS_TAB)} />
            </TabPanel>
            <TabPanel value={value} index={1}>
                <AppsTab baseId={build(baseId, ids.APPS_TAB)} />
            </TabPanel>
            <TabPanel value={value} index={2}>
                <UsersTab baseId={build(baseId, ids.USERS_TAB)} />
            </TabPanel>
        </div>
    );
}

export default withI18N(NavTabs, myMessagesFile);
