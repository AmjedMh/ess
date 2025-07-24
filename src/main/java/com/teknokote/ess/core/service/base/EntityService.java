package com.teknokote.ess.core.service.base;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * Base service Interface for generic CRUD operations.
 *
 * @param <E>  The entity type
 * @param <I>  The ID type
 * @since 1.0
 */
public interface EntityService<E, I extends Serializable> {

	Optional<E> findById(I id);

	E findOne(I id);

	List<E> findAll();

	List<E> findAll(Sort sort);

	Page<E> findAll(Pageable pageable);

	boolean exists(I id);

	long count();

	void flush();

	E saveAndFlush(E entity);

	void deleteInBatch(Iterable<E> entities);

	E save(E entity);

	<S extends E> List<S> save(Iterable<S> entities);

	void delete(I id);

	void delete(E entity);

	void delete(Iterable<? extends E> entities);

	void deleteAll();

	E saveOnlyEntity(E entity);
}
