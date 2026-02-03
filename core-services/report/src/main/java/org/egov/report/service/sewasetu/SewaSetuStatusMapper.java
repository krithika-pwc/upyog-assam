package org.egov.report.service.sewasetu;

import java.util.HashMap;
import java.util.Map;

/**
 * Maps our application statuses to Sewa Setu status codes.
 * <p>
 * Sewa Setu codes: P=Pending, R=Rejected, D=Delivered, S=Submitted, F=Forwarded, QS=Query Sent.
 * <p>
 * Status mapping – confirm with business where marked PENDING:
 * <ul>
 *   <li>D (Delivered): APPLICATION_COMPLETED – mapped</li>
 *   <li>R (Rejected): REJECTED – mapped</li>
 *   <li>F (Forwarded): FORWARDED_TO_DD_AD_TCP, FORWARDED_TO_TECHNICAL_ENGINEER – mapped</li>
 *   <li>QS (Query Sent): SEND_TO_CITIZEN – mapped</li>
 *   <li>P (Pending): PAYMENT_PENDING, PENDING_DA_ENGINEER, PENDING_CHAIRMAN_DA, PENDING_CHAIRMAN_PRESIDENT,
 *       PENDING_DD_AD_DEVELOPMENT_AUTHORITY, PENDING_FOR_SCRUTINY, PENDING_RTP_APPROVAL, GIS_VALIDATION, EDIT_APPLICATION – PENDING (confirm P vs S)</li>
 *   <li>S (Submitted): CITIZEN_APPROVAL, CITIZEN_FINAL_PAYMENT – PENDING (confirm S vs P)</li>
 * </ul>
 */
public final class SewaSetuStatusMapper {

    private static final Map<String, String> APPLICATION_STATUS_TO_SEWA_SETU = new HashMap<>();

    static {
        // Delivered – application completed
        APPLICATION_STATUS_TO_SEWA_SETU.put("APPLICATION_COMPLETED", "D");

        // Rejected
        APPLICATION_STATUS_TO_SEWA_SETU.put("REJECTED", "R");

        // Forwarded (various forwarded states)
        APPLICATION_STATUS_TO_SEWA_SETU.put("FORWARDED_TO_DD_AD_TCP", "F");
        APPLICATION_STATUS_TO_SEWA_SETU.put("FORWARDED_TO_TECHNICAL_ENGINEER", "F");

        // Query Sent – sent back to citizen
        APPLICATION_STATUS_TO_SEWA_SETU.put("SEND_TO_CITIZEN", "QS");

        // Pending – various pending states (mapped to P; confirm with business)
        APPLICATION_STATUS_TO_SEWA_SETU.put("PAYMENT_PENDING", "P");
        APPLICATION_STATUS_TO_SEWA_SETU.put("PENDING_DA_ENGINEER", "P");
        APPLICATION_STATUS_TO_SEWA_SETU.put("PENDING_CHAIRMAN_DA", "P");
        APPLICATION_STATUS_TO_SEWA_SETU.put("PENDING_CHAIRMAN_PRESIDENT", "P");
        APPLICATION_STATUS_TO_SEWA_SETU.put("PENDING_DD_AD_DEVELOPMENT_AUTHORITY", "P");
        APPLICATION_STATUS_TO_SEWA_SETU.put("PENDING_FOR_SCRUTINY", "P");
        APPLICATION_STATUS_TO_SEWA_SETU.put("PENDING_RTP_APPROVAL", "P");
        APPLICATION_STATUS_TO_SEWA_SETU.put("GIS_VALIDATION", "P");
        APPLICATION_STATUS_TO_SEWA_SETU.put("EDIT_APPLICATION", "P");

        // Submitted / Citizen action (mapped to S; confirm with business)
        APPLICATION_STATUS_TO_SEWA_SETU.put("CITIZEN_APPROVAL", "S");
        APPLICATION_STATUS_TO_SEWA_SETU.put("CITIZEN_FINAL_PAYMENT", "S");
    }

    private SewaSetuStatusMapper() {}

    /**
     * Returns Sewa Setu status code for our application status, or null if not mapped.
     */
    public static String toSewaSetuCode(String ourApplicationStatus) {
        if (ourApplicationStatus == null || ourApplicationStatus.isEmpty()) {
            return null;
        }
        return APPLICATION_STATUS_TO_SEWA_SETU.get(ourApplicationStatus.trim());
    }
}
