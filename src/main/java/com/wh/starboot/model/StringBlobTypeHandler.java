package com.wh.starboot.model;

import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;

import java.io.ByteArrayInputStream;
import java.sql.*;

/**
 * handle Blob as String<br/>
 * Created by kingbo on 2016/11/24.
 */
public class StringBlobTypeHandler extends BaseTypeHandler<String> {

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, String parameter, JdbcType jdbcType) throws SQLException {
        byte[] bytes = parameter.getBytes();
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        ps.setBinaryStream(i, bis, bytes.length);
    }

    @Override
    public String getNullableResult(ResultSet rs, String columnName) throws SQLException {
        Blob blob = rs.getBlob(columnName);
        String returnValue = null;
        if (null != blob) {
            returnValue = new String(blob.getBytes(1L, (int) blob.length()));
        }
        return returnValue;
    }

    @Override
    public String getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        Blob blob = rs.getBlob(columnIndex);
        String returnValue = null;
        if (null != blob) {
            returnValue = new String(blob.getBytes(1L, (int) blob.length()));
        }
        return returnValue;
    }

    @Override
    public String getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        Blob blob = cs.getBlob(columnIndex);
        String returnValue = null;
        if (null != blob) {
            returnValue = new String(blob.getBytes(1L, (int) blob.length()));
        }
        return returnValue;
    }

}
