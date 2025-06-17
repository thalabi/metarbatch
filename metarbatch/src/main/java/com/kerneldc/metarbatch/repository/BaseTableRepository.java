package com.kerneldc.metarbatch.repository;

import java.io.Serializable;
import java.util.List;

import jakarta.transaction.Transactional;

import org.springframework.data.repository.NoRepositoryBean;

import com.kerneldc.metarbatch.domain.AbstractPersistableEntity;
import com.kerneldc.metarbatch.domain.LogicalKeyHolder;

@NoRepositoryBean
public interface BaseTableRepository<T extends AbstractPersistableEntity, ID extends Serializable> extends BaseEntityRepository<T, ID> {	

	List<T> findByLogicalKeyHolder(LogicalKeyHolder logicalKeyHolder);
	
	Long deleteByLogicalKeyHolder(LogicalKeyHolder logicalKeyHolder);
	
	@Transactional
	default <E extends T> void deleteListByLogicalKeyHolder(List<E> entities) {
		entities.forEach(entity -> deleteByLogicalKeyHolder(entity.getLogicalKeyHolder()));
	}
	
	@Transactional
	default <E extends T> List<E> saveAllTransaction(Iterable<E> entities) {
		return saveAll(entities);
	}
	
	@Transactional
	default <E extends T> E saveTransaction(E entity) {
		return save(entity);
	}
}
