package io.cornerstone.core.web;

import javax.validation.Valid;

import com.fasterxml.jackson.annotation.JsonView;
import io.cornerstone.core.domain.Versioned;
import io.cornerstone.core.domain.View;
import io.cornerstone.core.util.BeanUtils;

import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

public abstract class AbstractEntityController<T, ID> extends AbstractReadableEntityController<T, ID> {

	@PostMapping(PATH_LIST)
	public T save(@RequestBody @JsonView(View.Creation.class) @Valid T entity) {
		beforeSave(entity);
		return this.repository.save(entity);
	}

	@PutMapping(PATH_DETAIL)
	public void update(@PathVariable ID id, @RequestBody @JsonView(View.Update.class) @Valid T entity) {
		beforeUpdate(id, entity);
		this.repository.findById(id).map(en -> {
			BeanUtils.copyPropertiesInJsonView(entity, en, determineViewForUpdate(entity));
			return this.repository.save(en);
		}).orElseThrow(() -> notFound(id));
	}

	@PatchMapping(PATH_DETAIL)
	public T patch(@PathVariable ID id, @RequestBody @JsonView(View.Update.class) @Valid T entity) {
		beforePatch(id, entity);
		return this.repository.findById(id).map(en -> {
			BeanUtils.copyNonNullProperties(entity, en);
			return this.repository.save(en);
		}).orElseThrow(() -> notFound(id));
	}

	@DeleteMapping(PATH_DETAIL)
	public void delete(@PathVariable ID id) {
		T entity = this.repository.findById(id).orElseThrow(() -> notFound(id));
		beforeDelete(entity);
		this.repository.delete(entity);
	}

	protected void beforeSave(T entity) {

	}

	protected void beforeUpdate(ID id, T entity) {

	}

	protected void beforePatch(ID id, T entity) {

	}

	protected void beforeDelete(T entity) {

	}

	protected Class<?> determineViewForUpdate(T entity) {
		return findViewForEntity(((entity instanceof Versioned versioned) && versioned.getVersion() == null)
				? View.Edit.class : View.Update.class, this.entityClass);
	}

	private static Class<?> findViewForEntity(Class<?> defaultView, Class<?> entityClass) {
		String entityViewName = entityClass.getName() + "$View$" + defaultView.getSimpleName();
		if (ClassUtils.isPresent(entityViewName, entityClass.getClassLoader())) {
			try {
				return ClassUtils.forName(entityViewName, entityClass.getClassLoader());
			}
			catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		}
		return defaultView;
	}

}
