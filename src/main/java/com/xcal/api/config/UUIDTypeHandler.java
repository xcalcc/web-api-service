/*
   Copyright (C) 2019-2022 Xcalibyte (Shenzhen) Limited.
   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at
     http://www.apache.org/licenses/LICENSE-2.0
   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package com.xcal.api.config;

import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;
import org.apache.ibatis.type.TypeHandler;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

@MappedTypes(UUID.class)
@MappedJdbcTypes(JdbcType.OTHER)
public class UUIDTypeHandler implements TypeHandler<UUID> {

	@Override
	public void setParameter(PreparedStatement preparedStatement, int i, UUID uuid, JdbcType jdbcType) throws SQLException {
		preparedStatement.setString(i, Optional.ofNullable(uuid).map(UUID::toString).orElse(null));
	}

	@Override
	public UUID getResult(ResultSet resultSet, String s) throws SQLException {
		return Optional.ofNullable(resultSet.getString(s)).map(UUID::fromString).orElse(null);
	}

	@Override
	public UUID getResult(ResultSet resultSet, int i) throws SQLException {
		return Optional.ofNullable(resultSet.getString(i)).map(UUID::fromString).orElse(null);
	}

	@Override
	public UUID getResult(CallableStatement callableStatement, int i) throws SQLException {
		return Optional.ofNullable(callableStatement.getString(i)).map(UUID::fromString).orElse(null);
	}

}
