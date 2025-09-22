import { useQuery } from "react-query"

import useBPAV2Search from "./useBPAV2Search";

const useOBPSV2Search = (selectedType, payload, tenantId, filters, params, config = {}) => {
    
        return useBPAV2Search(tenantId, filters, config);
    
}

export default useOBPSV2Search;