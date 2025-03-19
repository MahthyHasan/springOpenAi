package com.Task.Hasan.Repository;

import com.Task.Hasan.Entity.Requests;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RequestRepo extends MongoRepository<Requests, String> {

}
