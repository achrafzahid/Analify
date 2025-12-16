package com.analyfy.analify.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface BaseRepository<E,ID> extends JpaRepository<E,ID>{
    
}
