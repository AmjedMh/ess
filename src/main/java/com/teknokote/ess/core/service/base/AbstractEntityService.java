package com.teknokote.ess.core.service.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.List;
import java.util.Optional;


@Slf4j
public abstract class AbstractEntityService<E, I extends Serializable> implements EntityService<E, I> {

	private JpaRepository<E, I> repository;
// For now, java doesn't support new operation on generic types and the
	// reflection API force use to manually cast.
	protected AbstractEntityService(JpaRepository<E, I> repository) {
		this.repository = repository;
	}

	@Override
	@Transactional(readOnly = true)
	public Optional<E> findById(I id) {
		return repository.findById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public E findOne(I id) {
		Optional<E> o = repository.findById(id);
		return o.orElse(null);
	}

	@Override
	@Transactional(readOnly = true)
	public List<E> findAll() {
		return repository.findAll();
	}

	@Override
	@Transactional(readOnly = true)
	public List<E> findAll(Sort sort) {
		return repository.findAll(sort);
	}

	@Override
	@Transactional(readOnly = true)
	public Page<E> findAll(Pageable pageable) {
		return repository.findAll(pageable);
	}

	@Override
	@Transactional(readOnly = true)
	public boolean exists(I id) {
		return repository.existsById(id);
	}

	@Override
	public long count() {
		return repository.count();
	}

	@Override
	public void flush() {
		repository.flush();
	}

	@Override
	public E saveAndFlush(E entity) {
		return repository.saveAndFlush(entity);
	}

	@Override
	public E save(E entity) {
		return repository.save(entity);
	}

	@Override
	public <S extends E> List<S> save(Iterable<S> entities) {
		return repository.saveAll(entities);
	}

	@Override
	public void delete(I id) {
		repository.deleteById(id);
	}

	@Override
	public void delete(E entity) {
		repository.delete(entity);
	}

	@Override
	public void delete(Iterable<? extends E> entities) {
		repository.deleteAll(entities);
	}

	@Override
	public void deleteInBatch(Iterable<E> entities) {
		repository.deleteAllInBatch(entities);
	}

	@Override
	public void deleteAll() {
		repository.deleteAll();
	}

	@Override
	public E saveOnlyEntity(E entity) {
		return repository.save(entity);
	}

}
