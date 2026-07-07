
package io.cornerstone.core.persistence.type;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.usertype.AnnotationBasedUserType;
import org.hibernate.usertype.UserTypeCreationContext;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

import static java.sql.Types.VARCHAR;

@Slf4j
class JsonType implements AnnotationBasedUserType<Json, Object> {

	private static final JsonMapper jsonMapper = JsonMapper.builder()
		.changeDefaultPropertyInclusion(incl -> incl.withValueInclusion(JsonInclude.Include.NON_NULL))
		.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
		.build();

	private Type type;

	@Override
	public void initialize(Json annotation, UserTypeCreationContext context) {
		Member member = context.getMemberDetails().toJavaMember();
		if (member instanceof Field field) {
			this.type = field.getType();
		}
		else if (member instanceof Method method) {
			this.type = method.getGenericReturnType();
		}
		else {
			throw new IllegalStateException("");
		}
	}

	@Override
	public int getSqlType() {
		return VARCHAR;
	}

	@Override
	public Class<Object> returnedClass() {
		return Object.class;
	}

	@Override
	public int hashCode(Object x) {
		return x.hashCode();
	}

	@Override
	public Object nullSafeGet(ResultSet rs, int position, WrapperOptions options) throws SQLException {
		String string = rs.getString(position);
		if (rs.wasNull()) {
			return null;
		}
		if (this.type == String.class) {
			return string;
		}
		return jsonMapper.readValue(string, jsonMapper.constructType(this.type));
	}

	@Override
	public void nullSafeSet(PreparedStatement st, Object value, int index, WrapperOptions options) throws SQLException {
		if (value == null) {
			st.setNull(index, VARCHAR);
		}
		st.setString(index, this.type == String.class ? (String) value : jsonMapper.writeValueAsString(value));
	}

	@Override
	public boolean isMutable() {
		return true;
	}

	@Override
	public Object deepCopy(Object value) {
		if (value == null || this.type == String.class) {
			return value;
		}
		return jsonMapper.readValue(jsonMapper.writeValueAsString(value), jsonMapper.constructType(this.type));
	}

	@Override
	public Serializable disassemble(Object value) {
		return (Serializable) value;
	}

	@Override
	public Object assemble(Serializable cached, Object owner) {
		return cached;
	}

}
