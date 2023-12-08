package io.cornerstone.core.hibernate.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.OracleDialect;
import org.hibernate.engine.jdbc.spi.JdbcServices;
import org.hibernate.query.sqm.internal.SqmCriteriaNodeBuilder;
import org.hibernate.query.sqm.tree.expression.SqmLiteral;
import org.hibernate.query.sqm.tree.predicate.SqmBooleanExpressionPredicate;
import org.hibernate.query.sqm.tree.predicate.SqmNegatedPredicate;
import org.hibernate.query.sqm.tree.predicate.SqmNullnessPredicate;

import org.springframework.data.domain.Example;
import org.springframework.data.jpa.convert.QueryByExamplePredicateBuilder;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

public class PredicateBuilder {

	public static boolean isConstantTrue(Predicate predicate) {
		if (predicate == null) {
			return true;
		}
		return switch (predicate) {
			case SqmNegatedPredicate np -> isConstantFalse(np.getWrappedPredicate());
			case SqmNullnessPredicate np -> {
				if (np.getExpression() instanceof SqmLiteral<?> literal) {
					yield np.isNegated() && literal.getLiteralValue() != null
							|| !np.isNegated() && literal.getLiteralValue() == null;
				}
				else {
					yield false;
				}
			}
			case SqmBooleanExpressionPredicate bep -> {
				if (bep.getBooleanExpression() instanceof SqmLiteral<Boolean> literal) {
					yield !bep.isNegated() && literal.getLiteralValue()
							|| bep.isNegated() && !literal.getLiteralValue();
				}
				else {
					yield false;
				}
			}
			default -> {
				yield false;
			}
		};

	}

	public static boolean isConstantFalse(Predicate predicate) {
		if (predicate == null) {
			return false;
		}
		return switch (predicate) {
			case SqmNegatedPredicate np -> isConstantTrue(np.getWrappedPredicate());
			case SqmNullnessPredicate np -> {
				if (np.getExpression() instanceof SqmLiteral<?> literal) {
					yield np.isNegated() && literal.getLiteralValue() == null
							|| !np.isNegated() && literal.getLiteralValue() != null;
				}
				else {
					yield false;
				}
			}
			case SqmBooleanExpressionPredicate bep -> {
				if (bep.getBooleanExpression() instanceof SqmLiteral<Boolean> literal) {
					yield !bep.isNegated() && !literal.getLiteralValue()
							|| bep.isNegated() && literal.getLiteralValue();

				}
				else {
					yield false;
				}
			}
			default -> {
				yield false;
			}
		};
	}

	public static <T> Predicate andExample(Root<T> root, CriteriaBuilder cb, Predicate predicate, Example<T> example) {
		Predicate examplePredicate = QueryByExamplePredicateBuilder.getPredicate(root, cb, example);
		if (isConstantTrue(examplePredicate)) {
			return predicate;
		}
		return cb.and(predicate, examplePredicate);
	}

	public static <T> Predicate orExample(Root<T> root, CriteriaBuilder cb, Predicate predicate, Example<T> example) {
		Predicate examplePredicate = QueryByExamplePredicateBuilder.getPredicate(root, cb, example);
		if (isConstantTrue(examplePredicate)) {
			return examplePredicate;
		}
		return cb.or(predicate, examplePredicate);
	}

	public static <T> Predicate contains(Root<T> root, CriteriaBuilder cb, String propertyName, String item) {
		Assert.doesNotContain(item, ",", "'item' should not contains comma");
		Dialect dialect = getDialect(cb);
		if (dialect instanceof MySQLDialect) {
			return cb.greaterThan(cb.function("find_in_set", Integer.class, cb.literal(item), root.get(propertyName)),
					0);
		}
		// @formatter:off
		/** https://hibernate.atlassian.net/browse/HHH-16419
		if (dialect instanceof PostgreSQLDialect) {
			return cb.equal(cb.literal(item), cb.function("any", String.class,
					cb.function("string_to_array", String[].class, root.get(propertyName), cb.literal(","))));
		}
		*/
		// @formatter:on
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
		if (dialect instanceof OracleDialect) {
			return cb.gt(cb.function("regexp_instr", Integer.class, root.get(propertyName), cb.literal(pattern)), 0);
		}
		// for mysql 5.7:
		// create function regexp_like (text varchar(255), pattern varchar(255)) returns
		// integer deterministic return text regexp pattern;
		// for postgresql:
		// create or replace function regexp_like(character varying,character varying)
		// returns integer as $$ select ($1 ~ $2)::int; $$ language sql immutable;
		if (dialect instanceof H2Dialect && dialect.getVersion().isSameOrAfter(2)) {
			return cb.equal(cb.function("regexp_like", Boolean.class, root.get(propertyName), cb.literal(pattern)),
					Boolean.TRUE);
		}
		return cb.equal(cb.function("regexp_like", Integer.class, root.get(propertyName), cb.literal(pattern)), 1);
	}

	@Nullable
	private static Dialect getDialect(CriteriaBuilder cb) {
		if (cb instanceof SqmCriteriaNodeBuilder scnb) {
			return scnb.getServiceRegistry().getService(JdbcServices.class).getDialect();
		}
		return null;
	}

}
