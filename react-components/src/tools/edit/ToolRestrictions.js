import React from "react";

import ids from "../ids";
import SimpleExpansionPanel from "../SimpleExpansionPanel";

import {
    build,
    FormCheckbox,
    FormNumberField,
    FormSelectField,
    getMessage,
} from "@cyverse-de/ui-lib";

import { MenuItem, Typography } from "@material-ui/core";
import { Field, getIn } from "formik";
import numeral from "numeral";
import PropTypes from "prop-types";

const NETWORK_MODES = ["bridge", "none"];

const ONE_GB = 1024 * 1024 * 1024;

const formatGBListItem = (size) => size && numeral(size).format("0 ib");
const formatGBValue = (size) => size && numeral(size).format("0.0 ib");

function buildLimitList(startValue, maxValue) {
    let limits = [];
    limits.push(0);
    limits.push(startValue);

    let value = startValue;
    while (value < maxValue) {
        value *= 2;
        limits.push(value);
    }
    return limits;
}

function Restrictions(props) {
    const {
        parentId,
        isAdmin,
        maxCPUCore,
        maxMemory,
        maxDiskSpace,
        form: { values },
    } = props;

    const maxCPUCoreList = buildLimitList(1, maxCPUCore);
    const memoryLimitList = buildLimitList(2 * ONE_GB, maxMemory);
    const minDiskSpaceList = buildLimitList(ONE_GB, maxDiskSpace);

    const validateMinCPUs = (value) => {
        if (value && value < 0) {
            return getMessage("validationErrMustBePositive");
        }

        const max_cpu_cores = getIn(values, "container.max_cpu_cores");
        if (max_cpu_cores > 0 && value > max_cpu_cores) {
            return getMessage("validationErrMinCPUsGreaterThanMax");
        }
    };

    const validateMaxCPUs = (value) => {
        if (value && value < 0) {
            return getMessage("validationErrMustBePositive");
        }

        const min_cpu_cores = getIn(values, "container.min_cpu_cores");
        if (0 < value && value < min_cpu_cores) {
            return getMessage("validationErrMaxCPUsLessThanMin");
        }
    };

    const validateMinRAM = (value) => {
        if (value && value < 0) {
            return getMessage("validationErrMustBePositive");
        }

        const memory_limit = getIn(values, "container.memory_limit");
        if (memory_limit > 0 && value > memory_limit) {
            return getMessage("validationErrMinRAMGreaterThanMax");
        }
    };

    const validateMaxRAM = (value) => {
        if (value && value < 0) {
            return getMessage("validationErrMustBePositive");
        }

        const min_memory_limit = getIn(values, "container.min_memory_limit");
        if (0 < value && value < min_memory_limit) {
            return getMessage("validationErrMaxRAMLessThanMin");
        }
    };

    return (
        <SimpleExpansionPanel
            header={getMessage("restrictions")}
            parentId={parentId}
        >
            {isAdmin && (
                <Field
                    name="restricted"
                    label={getMessage("restricted")}
                    id={build(parentId, ids.EDIT_TOOL_DLG.RESTRICTED)}
                    component={FormCheckbox}
                />
            )}
            {isAdmin && (
                <Field
                    name="container.min_cpu_cores"
                    label={getMessage("minCPUCores")}
                    id={build(parentId, ids.EDIT_TOOL_DLG.MIN_CPU_CORES)}
                    component={FormNumberField}
                    validate={validateMinCPUs}
                />
            )}
            <Field
                name="container.max_cpu_cores"
                label={getMessage("maxCPUCores")}
                id={build(parentId, ids.EDIT_TOOL_DLG.MAX_CPU_CORES)}
                component={FormSelectField}
                validate={validateMaxCPUs}
            >
                {maxCPUCoreList.map((size, index) => (
                    <MenuItem
                        key={index}
                        value={size}
                        id={build(
                            parentId,
                            ids.EDIT_TOOL_DLG.MAX_CPU_CORES,
                            size
                        )}
                    >
                        {size}
                    </MenuItem>
                ))}
            </Field>
            {isAdmin && (
                <Field
                    name="container.min_memory_limit"
                    label={getMessage("minMemoryLimit")}
                    id={build(parentId, ids.EDIT_TOOL_DLG.MIN_MEMORY_LIMIT)}
                    component={FormNumberField}
                    validate={validateMinRAM}
                />
            )}
            <Field
                name="container.memory_limit"
                label={getMessage("memoryLimit")}
                id={build(parentId, ids.EDIT_TOOL_DLG.MEMORY_LIMIT)}
                component={FormSelectField}
                renderValue={formatGBValue}
                validate={validateMaxRAM}
            >
                {memoryLimitList.map((size, index) => (
                    <MenuItem
                        key={index}
                        value={size}
                        id={build(
                            parentId,
                            ids.EDIT_TOOL_DLG.MEMORY_LIMIT,
                            size
                        )}
                    >
                        {formatGBListItem(size)}
                    </MenuItem>
                ))}
            </Field>
            {isAdmin && (
                <Field
                    name="container.cpu_shares"
                    label={getMessage("cpuShares")}
                    id={build(parentId, ids.EDIT_TOOL_DLG.CPU_SHARES)}
                    component={FormNumberField}
                />
            )}
            <Field
                name="container.min_disk_space"
                label={getMessage("minDiskSpace")}
                id={build(parentId, ids.EDIT_TOOL_DLG.MIN_DISK_SPACE)}
                component={FormSelectField}
                renderValue={formatGBValue}
            >
                {minDiskSpaceList.map((size, index) => (
                    <MenuItem
                        key={index}
                        value={size}
                        id={build(
                            parentId,
                            ids.EDIT_TOOL_DLG.MIN_DISK_SPACE,
                            size
                        )}
                    >
                        {formatGBListItem(size)}
                    </MenuItem>
                ))}
            </Field>
            {!isAdmin && (
                <Typography variant="caption">
                    {getMessage("restrictionsSupport")}
                </Typography>
            )}
            <Field
                name="container.pids_limit"
                label={getMessage("pidsLimit")}
                id={build(parentId, ids.EDIT_TOOL_DLG.PIDS_LIMIT)}
                disabled={!isAdmin}
                component={FormNumberField}
            />
            <Field
                name="container.network_mode"
                label={getMessage("networkMode")}
                id={build(parentId, ids.EDIT_TOOL_DLG.NETWORK_MODE)}
                disabled={!isAdmin}
                component={FormSelectField}
            >
                {NETWORK_MODES.map((mode, index) => (
                    <MenuItem
                        key={index}
                        value={mode}
                        id={build(
                            parentId,
                            ids.EDIT_TOOL_DLG.NETWORK_MODE,
                            mode
                        )}
                    >
                        {mode}
                    </MenuItem>
                ))}
            </Field>
            <Field
                name="time_limit_seconds"
                label={getMessage("timeLimit")}
                id={build(parentId, ids.EDIT_TOOL_DLG.TIME_LIMIT)}
                disabled={!isAdmin}
                component={FormNumberField}
            />
        </SimpleExpansionPanel>
    );
}

Restrictions.propTypes = {
    isAdmin: PropTypes.bool.isRequired,
    parentId: PropTypes.string.isRequired,
    maxCPUCore: PropTypes.number.isRequired,
    maxDiskSpace: PropTypes.number.isRequired,
    maxMemory: PropTypes.number.isRequired,
};

export default Restrictions;
