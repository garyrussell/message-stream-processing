package com.lukeshannon.datastreaming;

import javax.transaction.Transactional;

import org.springframework.data.repository.CrudRepository;

@Transactional
public interface MessageRepo extends CrudRepository<Message, Long> {

}
