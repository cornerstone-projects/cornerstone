package io.cornerstone.core.hibernate.criteria;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.PostgreSQL81Dialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.query.criteria.internal.CriteriaBuilderImpl;
import org.hibernate.query.criteria.internal.expression.LiteralExpression;
import org.hibernate.query.criteria.internal.predicate.BooleanAssertionPredicate;
import org.hibernate.query.criteria.internal.predicate.NegatedPredicateWrapper;
import org.hibernate.query.criteria.internal.predicate.NullnessPredicate;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import io.cornerstone.core.util.ReflectionUtils;

public class PredicateBuilder {

	public static boolean isConstantTrue(Predicate predicate) {
		if (predicate instanceof NegatedPredicateWrapper) {
			return isConstantFalse(unwrap((NegatedPredicateWrapper) predicate));
		} else if (predicate instanceof NullnessPredicate) {
			// literal cannot be null
			return false;
		} else if (predicate instanceof BooleanAssertionPredicate) {
			BooleanAssertionPredicate bap = (BooleanAssertionPredicate) predicate;
			Expression<Boolean> exp = bap.getExpression();
			if (exp instanceof LiteralExpression) {
				return bap.getAssertedValue().equals(((LiteralExpression<Boolean>) exp).getLiteral());
			}
		}
		return false;
	}

	public static boolean isConstantFalse(Predicate predicate) {
		if (predicate instanceof NegatedPredicateWrapper) {
			return isConstantTrue(unwrap((NegatedPredicateWrapper) predicate));
		} else if (predicate instanceof NullnessPredicate) {
			return ((NullnessPredicate) predicate).getOperand() instanceof LiteralExpression;
		} else if (predicate instanceof BooleanAssertionPredicate) {
			BooleanAssertionPredicate bap = (BooleanAssertionPredicate) predicate;
			Expression<Boolean> exp = bap.getExpression();
			if (exp instanceof LiteralExpression) {
				return !bap.getAssertedValue().equals(((LiteralExpression<Boolean>) exp).getLiteral());
			}
		}
		return false;
	}

	public static <T> Predicate andExample(Root<T> root, CriteriaBuilder cb, Predicate predicate, Example<T> example) {
		Predicate examplePredicate = QueryByExamplePredicateBuilder.getPredicate(root, cb, example);
		if (isConstantTrue(examplePredicate)) // eliminate unnecessary true=1
			return predicate;
		return cb.and(predicate, examplePredicate);
	}

	public static <T> Predicate orExample(Root<T> root, CriteriaBuilder cb, Predicate predicate, Example<T> example) {
		Predicate examplePredicate = QueryByExamplePredicateBuilder.getPredicate(root, cb, example);
		if (isConstantTrue(examplePredicate))
			return examplePredicate;
		return cb.or(predicate, examplePredicate);
	}

	public static <T> Predicate contains(Root<T> root, CriteriaBuilder cb, String propertyName, String item) {
		Assert.doesNotContain(item, ",", "'item' should not contains comma");
		Dialect dialect = getDialect(cb);
		if (dialect instanceof MySQLDialect)
			return cb.greaterThan(cb.function("find_in_set", Integer.class, cb.literal(item), root.get(propertyName)),
					0);
		if (dialect instanceof PostgreSQL81Dialect)
			return cb.equal(cb.literal(item), cb.function("any", String.class,
					cb.function("string_to_array", String[].class, root.get(propertyName), cb.literal(","))));
		return cb.like(cb.concat(cb.concat(",", root.get(propertyName)), ","), '%' + item + '%');
	}

	public static <T> Predicate itemContains(Root<T> root, CriteriaBuilder cb, String propertyName, String value) {
		Assert.doesNotContain(value, ",", "'value' should not contains comma");
		return cb.like(cb.concat(",", cb.concat(root.get(propertyName), ",")), "%,%" + value + "%,%");
	}

	public static <T> Predicate itemStartsWith(Root<T> root, CriteriaBuilder cb, String propertyName, String prefix) {
		Assert.doesNotContain(prefix, ",", "'prefix' should not contains comma");
		return cb.like(cb.concat(",", root.get(propertyName)), "%," + prefix + '%');
	}

	public static <T> Predicate itemEndsWith(Root<T> root, CriteriaBuilder cb, String propertyName, String suffix) {
		Assert.doesNotContain(suffix, ",", "'suffix' should not contains comma");
		return cb.like(cb.concat(root.get(propertyName), ","), '%' + suffix + ",%");
	}

	public static <T> Predicate regexpLike(Root<T> root, CriteriaBuilder cb, String propertyName, String pattern) {
		Dialect dialect = getDialect(cb);
		if (dialect instanceof Oracle8iDialect) {
			return cb.gt(cb.function("regexp_instr", Integer.class, root.get(propertyName), cb.literal(pattern)), 0);
		}
		// for mysql 5.7:
		// create function regexp_like (text varchar(255), pattern varchar(255)) returns
		// integer deterministic return text regexp pattern;
		// for postgresql:
		// create or replace function regexp_like(character varying,character varying)
		// returns integer as $$ select ($1 ~ $2)::int; $$ language sql immutable;
		return cb.equal(cb.function("regexp_like", Integer.class, root.get(propertyName), cb.literal(pattern)), 1);
	}

	@Nullable
	private static Dialect getDialect(CriteriaBuilder cb) {
		if (cb instanceof CriteriaBuilderImpl) {
			return ((CriteriaBuilderImpl) cb).getEntityManagerFactory().getServiceRegistry()
					.getService(JdbcServices.class).getDialect();
		}
		return null;
	}

	private static Predicate unwrap(NegatedPredicateWrapper wrapper) {
		return ReflectionUtils.getFieldValue(wrapper, "predicate");
	}

}
