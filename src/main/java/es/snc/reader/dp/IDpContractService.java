package es.snc.reader.dp;

import java.util.List;

import es.snc.common.persistence.PaginatedList;
import es.snc.common.persistence.filter.FilterWithDate;
import es.snc.dp.dto.ContractDto;

public interface IDpContractService {

	PaginatedList<ContractDto> searchContractByFilter(String token, FilterWithDate filter);
}
