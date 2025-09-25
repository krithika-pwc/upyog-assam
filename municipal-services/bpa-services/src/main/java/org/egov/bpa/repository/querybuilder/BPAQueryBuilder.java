package org.egov.bpa.repository.querybuilder;

import org.egov.bpa.config.BPAConfiguration;
import org.egov.bpa.web.model.BPASearchCriteria;
import org.egov.common.utils.MultiStateInstanceUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

@Component
public class BPAQueryBuilder {

    @Autowired
    private MultiStateInstanceUtil centralInstanceUtil;

    @Autowired
    private BPAConfiguration config;

    private static final String LEFT_OUTER_JOIN = " LEFT OUTER JOIN ";

    //Updated base query with snake_case + new table names
    private static final String QUERY =
            "SELECT bpa.*, " +
                    "       bpadoc.*, " +
                    "       rtp.*, " +
                    "       area.*, " +
                    "       bpa.id AS bpa_id, " +
                    "       bpa.tenant_id AS bpa_tenant_id, " +
                    "       bpa.last_modified_time AS bpa_last_modified_time, " +
                    "       bpa.created_by AS bpa_created_by, " +
                    "       bpa.last_modified_by AS bpa_last_modified_by, " +
                    "       bpa.created_time AS bpa_created_time, " +
                    "       bpa.additional_details AS bpa_additional_details, " +
                    "       bpa.land_id AS bpa_land_id, " +

                    "       bpadoc.id AS bpa_doc_id, " +
                    "       bpadoc.additional_details AS doc_details, " +
                    "       bpadoc.document_type AS bpa_doc_document_type, " +
                    "       bpadoc.filestore_id AS bpa_doc_filestore, " +

                    "       rtp.id AS id, " +
                    "       rtp.buildingplan_id AS buildingplan_id, " +
                    "       rtp.rtp_id AS rtp_id, " +
                    "       rtp.rtp_category AS rtp_category, " +
                    "       rtp.rtp_name AS rtp_name, " +
                    "       rtp.assignment_status AS rtp_assignment_status, " +
                    "       rtp.assignment_date AS rtp_assignment_date, " +
                    "       rtp.changed_date AS rtp_changed_date, " +
                    "       rtp.remarks AS rtp_remarks, " +
                    "       rtp.additional_details AS rtp_additional_details, " +

                    "       area.id AS area_id, " +
                    "       area.district AS area_district, " +
                    "       area.planning_area AS area_planning_area, " +
                    "       area.planning_permit_authority AS area_planning_permit_authority, " +
                    "       area.building_permit_authority AS area_building_permit_authority, " +
                    "       area.revenue_village AS area_revenue_village, " +
                    "       area.mouza AS area_mouza, " +
                    "       area.ward AS area_ward " +

                    "FROM ug_bpa_buildingplans bpa " +
                    "LEFT OUTER JOIN ug_bpa_documents bpadoc ON bpadoc.buildingplan_id = bpa.id " +
                    "LEFT OUTER JOIN ug_bpa_rtp_detail rtp ON rtp.buildingplan_id = bpa.id " +
                    "LEFT OUTER JOIN ug_bpa_area_mapping_detail area ON area.buildingplan_id = bpa.id";

    private final String paginationWrapper = "SELECT * FROM " +
            "(SELECT *, DENSE_RANK() OVER (ORDER BY bpa_last_modified_time DESC) offset_ FROM ({}) result) result_offset " +
            "WHERE offset_ > ? AND offset_ <= ?";

    private final String countWrapper = "SELECT COUNT(DISTINCT(bpa_id)) FROM ({INTERNAL_QUERY}) AS bpa_count";

    /**
     * To build the search query based on given criteria
     */
    public String getBPASearchQuery(BPASearchCriteria criteria, List<Object> preparedStmtList, List<String> edcrNos, boolean isCount) {

        StringBuilder builder = new StringBuilder(QUERY);

        if (criteria.getTenantId() != null) {
            if (criteria.getTenantId().split("\\.").length == 1) {
                addClauseIfRequired(preparedStmtList, builder);
                builder.append(" bpa.tenant_id LIKE ?");
                preparedStmtList.add('%' + criteria.getTenantId() + '%');
            } else {
                addClauseIfRequired(preparedStmtList, builder);
                builder.append(" bpa.tenant_id = ? ");
                preparedStmtList.add(criteria.getTenantId());
            }
        }

        List<String> ids = criteria.getIds();
        if (!CollectionUtils.isEmpty(ids)) {
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" bpa.id IN (").append(createQuery(ids)).append(")");
            addToPreparedStatement(preparedStmtList, ids);
        }

        String edcrNumber = criteria.getEdcrNumber();
        if (edcrNumber != null) {
            List<String> edcrNumbers = Arrays.asList(edcrNumber.split(","));
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" bpa.edcr_number IN (").append(createQuery(edcrNumbers)).append(")");
            addToPreparedStatement(preparedStmtList, edcrNumbers);
        }

        String applicationNo = criteria.getApplicationNo();
        if (applicationNo != null && !applicationNo.trim().isEmpty()) {
            List<String> applicationNos = Arrays.asList(applicationNo.split(","));
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" bpa.application_no IN (").append(createQuery(applicationNos)).append(")");
            addToPreparedStatement(preparedStmtList, applicationNos);
        }

        String approvalNo = criteria.getApprovalNo();
        if (approvalNo != null) {
            List<String> approvalNos = Arrays.asList(approvalNo.split(","));
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" bpa.approval_no IN (").append(createQuery(approvalNos)).append(")");
            addToPreparedStatement(preparedStmtList, approvalNos);
        }

        String status = criteria.getStatus();
        if (status != null) {
            List<String> statuses = Arrays.asList(status.split(","));
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" bpa.status IN (").append(createQuery(statuses)).append(")");
            addToPreparedStatement(preparedStmtList, statuses);
        }

        String applicationType = criteria.getApplicationType();
        if (applicationType != null) {
            List<String> applicationTypes = Arrays.asList(applicationType.split(","));
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" bpa.application_type IN (").append(createQuery(applicationTypes)).append(")");
            addToPreparedStatement(preparedStmtList, applicationTypes);
        }

        String riskType = criteria.getRiskType();
        if (riskType != null) {
            List<String> riskTypes = Arrays.asList(riskType.split(","));
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" bpa.risk_type IN (").append(createQuery(riskTypes)).append(")");
            addToPreparedStatement(preparedStmtList, riskTypes);
        }

        Long approvalDate = criteria.getApprovalDate();
        if (approvalDate != null) {
            Calendar permitDate = Calendar.getInstance();
            permitDate.setTimeInMillis(approvalDate);

            int year = permitDate.get(Calendar.YEAR);
            int month = permitDate.get(Calendar.MONTH);
            int day = permitDate.get(Calendar.DATE);

            Calendar start = Calendar.getInstance();
            start.set(year, month, day, 0, 0, 0);

            Calendar end = Calendar.getInstance();
            end.set(year, month, day, 23, 59, 59);

            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" bpa.approval_date BETWEEN ")
                    .append(start.getTimeInMillis()).append(" AND ").append(end.getTimeInMillis());
        }

        if (criteria.getFromDate() != null && criteria.getToDate() != null) {
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" bpa.created_time BETWEEN ").append(criteria.getFromDate()).append(" AND ").append(criteria.getToDate());
        } else if (criteria.getFromDate() != null) {
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" bpa.created_time >= ").append(criteria.getFromDate());
        }

        List<String> businessService = criteria.getBusinessService();
        if (!CollectionUtils.isEmpty(businessService)) {
            addClauseIfRequired(preparedStmtList, builder);
            builder.append(" bpa.business_service IN (").append(createQuery(businessService)).append(")");
            addToPreparedStatement(preparedStmtList, businessService);
        }

        List<String> landId = criteria.getLandId();
        List<String> createdBy = criteria.getCreatedBy();
        if (!CollectionUtils.isEmpty(landId)) {
            addClauseIfRequired(preparedStmtList, builder);
            if (!CollectionUtils.isEmpty(createdBy)) {
                builder.append("(");
            }
            builder.append(" bpa.land_id IN (").append(createQuery(landId)).append(")");
            addToPreparedStatement(preparedStmtList, landId);
        }

        if (!CollectionUtils.isEmpty(createdBy)) {
            if (!CollectionUtils.isEmpty(landId)) {
                builder.append(" OR ");
            } else {
                addClauseIfRequired(preparedStmtList, builder);
            }
            builder.append(" bpa.created_by IN (").append(createQuery(createdBy)).append(")");
            if (!CollectionUtils.isEmpty(landId)) {
                builder.append(")");
            }
            addToPreparedStatement(preparedStmtList, createdBy);
        }

        if (isCount)
            return addCountWrapper(builder.toString());

        return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);
    }


    public String getBPASearchQueryForPlainSearch(BPASearchCriteria criteria, List<Object> preparedStmtList, List<String> edcrNos, boolean isCount) {

        StringBuilder builder = new StringBuilder(QUERY);

        if (criteria.getTenantId() != null) {
            if (centralInstanceUtil.isTenantIdStateLevel(criteria.getTenantId())) {

                addClauseIfRequired(preparedStmtList, builder);
                builder.append(" bpa.tenantid like ?");
                preparedStmtList.add('%' + criteria.getTenantId() + '%');
            } else {
                addClauseIfRequired(preparedStmtList, builder);
                builder.append(" bpa.tenantid=? ");
                preparedStmtList.add(criteria.getTenantId());
            }
        }


        if(isCount)
            return addCountWrapper(builder.toString());

        return addPaginationWrapper(builder.toString(), preparedStmtList, criteria);

    }

    private String addCountWrapper(String query) {
        return countWrapper.replace("{INTERNAL_QUERY}", query);
    }

    private String addPaginationWrapper(String query, List<Object> preparedStmtList, BPASearchCriteria criteria) {
        int limit = config.getDefaultLimit();
        int offset = config.getDefaultOffset();
        String finalQuery = paginationWrapper.replace("{}", query);

        if (criteria.getLimit() != null && criteria.getLimit() <= config.getMaxSearchLimit())
            limit = criteria.getLimit();

        if (criteria.getLimit() != null && criteria.getLimit() > config.getMaxSearchLimit()) {
            limit = config.getMaxSearchLimit();
        }

        if (criteria.getOffset() != null)
            offset = criteria.getOffset();

        if (limit == -1) {
            finalQuery = finalQuery.replace("WHERE offset_ > ? AND offset_ <= ?", "");
        } else {
            preparedStmtList.add(offset);
            preparedStmtList.add(limit + offset);
        }

        return finalQuery;
    }

    private void addClauseIfRequired(List<Object> values, StringBuilder queryString) {
        if (values.isEmpty())
            queryString.append(" WHERE ");
        else
            queryString.append(" AND ");
    }

    private void addToPreparedStatement(List<Object> preparedStmtList, List<String> ids) {
        preparedStmtList.addAll(ids);
    }

    private Object createQuery(List<String> ids) {
        StringBuilder builder = new StringBuilder();
        int length = ids.size();
        for (int i = 0; i < length; i++) {
            builder.append(" ?");
            if (i != length - 1)
                builder.append(",");
        }
        return builder.toString();
    }
}
