package es.snc.reader.security;

import es.snc.reader.dto.FilterUserDto;
import es.snc.security.dto.UserDto;
import es.snc.security.filter.UserFilter;

public interface ISecurityUserService {

	public UserDto findOneByFilter(FilterUserDto filter, String token);
}
