package com.cirio.primelog.repository;

import com.cirio.primelog.model.ErrorLog;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ErrorLogRepository extends MongoRepository<ErrorLog, String> {
    List<ErrorLog> findByUsuarioId(String usuarioId);
    long countByUsuarioId(String usuarioId);
}
